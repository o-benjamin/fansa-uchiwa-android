import { JUDGE_INSTRUCTION, JUDGE_RESPONSE_SCHEMA, parseJudgeAnswer } from "./moderation";
import type { JudgeCategory } from "./moderation";

const API_BASE = "https://generativelanguage.googleapis.com/v1beta/models";

/**
 * 画像を頼むときに、利用者の文章の前に付ける前置き。モデルを選ぶときに複数のモデルで同じ前置きを使って作り比べ、
 * この文で背景を切り抜きやすい絵になった。背景を単色にするのは、アプリが ML Kit で背景を切り抜くため。
 * 変えるときは、ML Kit で切り抜けるかを確かめ直す。
 */
export const STICKER_PREFIX =
  "ステッカー用のイラスト。ひとつだけ中央に大きく描く。フラットでかわいい画風、太めの白いふちどり、" +
  "背景は単色の薄いグレーで影なし（あとで背景を切り抜くため）。文字を入れる場合は指定された文字だけ。";

const SOURCE_IMAGE_NOTE = "添付の画像をもとに、同じ雰囲気のステッカーにする。";

/**
 * 待つ時間の上限。審査と画像を合わせても関数の制限時間（120秒。index.ts）に収め、
 * 時間切れで打ち切られて回数を戻せなくなることがないようにする。実際に試したときは、画像は数秒で返った。
 */
const JUDGE_TIMEOUT_MS = 15_000;
const IMAGE_TIMEOUT_MS = 60_000;

/**
 * モデルの側で止まった終わり方。利用者には `safety_blocked` で断り、回数は戻す。
 * RECITATION（既存の作品に似すぎた）も、利用者から見れば同じ「この内容では作れない」なので同じ扱いにする。
 */
const SAFETY_FINISH_REASONS = new Set([
  "SAFETY",
  "PROHIBITED_CONTENT",
  "BLOCKLIST",
  "SPII",
  "IMAGE_SAFETY",
  "IMAGE_PROHIBITED_CONTENT",
  "RECITATION",
]);

export interface SourceImage {
  mimeType: string;
  /** base64 */
  data: string;
}

export type ImageResult =
  | { kind: "image"; mimeType: string; data: string }
  | { kind: "safety_blocked"; finishReason: string }
  | { kind: "no_image"; finishReason: string };

/**
 * Gemini API がエラーを返した。`apiStatus` / `apiMessage` は API の答えの `error.status` / `error.message`
 * （キーが無効・モデル名が違う・割り当てを超えた、などを見分ける。利用者の文章は入らない）。
 */
export class GeminiHttpError extends Error {
  constructor(
    readonly status: number,
    readonly apiStatus?: string,
    readonly apiMessage?: string,
  ) {
    super(`Gemini API が ${status}（${apiStatus ?? "理由なし"}）を返した: ${apiMessage ?? ""}`);
  }
}

/**
 * 審査の結果。分類のほか、審査に送った文章や画像そのものが Gemini の安全フィルターで止められたら `safety_blocked`、
 * 答えを読めなければ undefined。
 */
export type JudgeResult = JudgeCategory | "safety_blocked" | undefined;

export interface GeminiClient {
  generateImage(model: string, prompt: string, sourceImage?: SourceImage): Promise<ImageResult>;
  /** 文章（と元画像）を審査する。 */
  judge(model: string, prompt: string, sourceImage?: SourceImage): Promise<JudgeResult>;
}

interface GenerateContentResponse {
  promptFeedback?: { blockReason?: string };
  candidates?: {
    finishReason?: string;
    content?: { parts?: { text?: string; inlineData?: { mimeType?: string; data?: string } }[] };
  }[];
}

/** 画像の生成の答えから、画像か、安全フィルターで止まったかを取り出す。 */
/** 頼んだ内容か答えが安全フィルターで止められていれば、その理由を返す。 */
function safetyStopReason(response: GenerateContentResponse): string | undefined {
  return (
    response.promptFeedback?.blockReason ??
    response.candidates?.map((candidate) => candidate.finishReason ?? "").find((reason) => SAFETY_FINISH_REASONS.has(reason))
  );
}

export function parseImageResponse(response: GenerateContentResponse): ImageResult {
  for (const candidate of response.candidates ?? []) {
    const image = candidate.content?.parts?.find((part) => part.inlineData?.data)?.inlineData;
    if (image?.data) return { kind: "image", mimeType: image.mimeType ?? "image/png", data: image.data };
  }
  const safetyReason = safetyStopReason(response);
  if (safetyReason) return { kind: "safety_blocked", finishReason: safetyReason };
  const finishReasons = (response.candidates ?? []).map((candidate) => candidate.finishReason ?? "なし");
  return { kind: "no_image", finishReason: finishReasons.join(",") || "候補なし" };
}

/** 元画像があれば、文章の前に置く（モデルは前にある画像を「添付の画像」として読む）。 */
function partsWith(text: string, sourceImage?: SourceImage): object[] {
  const parts: object[] = [{ text }];
  if (sourceImage) parts.unshift({ inlineData: { mimeType: sourceImage.mimeType, data: sourceImage.data } });
  return parts;
}

async function toHttpError(response: Response): Promise<GeminiHttpError> {
  const body = (await response.json().catch(() => undefined)) as { error?: { status?: string; message?: string } } | undefined;
  return new GeminiHttpError(response.status, body?.error?.status, body?.error?.message);
}

function firstText(response: GenerateContentResponse): string {
  return response.candidates?.[0]?.content?.parts?.find((part) => part.text)?.text ?? "";
}

/** Gemini API（Google AI Studio の API キー）を REST で呼ぶ。`fetchImpl` はテストで差し替える。 */
export function createGeminiClient(apiKey: string, fetchImpl: typeof fetch = fetch): GeminiClient {
  async function generateContent(model: string, body: object, timeoutMs: number): Promise<GenerateContentResponse> {
    const response = await fetchImpl(`${API_BASE}/${encodeURIComponent(model)}:generateContent`, {
      method: "POST",
      headers: { "content-type": "application/json", "x-goog-api-key": apiKey },
      body: JSON.stringify(body),
      signal: AbortSignal.timeout(timeoutMs),
    });
    if (!response.ok) throw await toHttpError(response);
    return (await response.json()) as GenerateContentResponse;
  }

  return {
    async generateImage(model, prompt, sourceImage) {
      const instruction = STICKER_PREFIX + (sourceImage ? SOURCE_IMAGE_NOTE : "") + "\n内容：" + prompt;
      const body = {
        contents: [{ role: "user", parts: partsWith(instruction, sourceImage) }],
        generationConfig: { responseModalities: ["IMAGE"], imageConfig: { aspectRatio: "1:1" } },
      };
      return parseImageResponse(await generateContent(model, body, IMAGE_TIMEOUT_MS));
    },

    async judge(model, prompt, sourceImage) {
      const body = {
        systemInstruction: { parts: [{ text: JUDGE_INSTRUCTION }] },
        contents: [{ role: "user", parts: partsWith(prompt, sourceImage) }],
        generationConfig: { temperature: 0, responseMimeType: "application/json", responseSchema: JUDGE_RESPONSE_SCHEMA },
      };
      const response = await generateContent(model, body, JUDGE_TIMEOUT_MS);
      if (safetyStopReason(response)) return "safety_blocked";
      return parseJudgeAnswer(firstText(response));
    },
  };
}
