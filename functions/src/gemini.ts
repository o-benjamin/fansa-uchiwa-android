import { JUDGE_INSTRUCTION, JUDGE_RESPONSE_SCHEMA, parseJudgeAnswer } from "./moderation";
import type { JudgeCategory } from "./moderation";

const API_BASE = "https://generativelanguage.googleapis.com/v1beta/models";

/**
 * 画像を頼むときに、利用者の文章の前に付ける前置き。作り比べ（2026-10-11）で使った文と同じ。
 * 背景を単色にするのは、アプリが ML Kit で背景を切り抜くため。
 */
export const STICKER_PREFIX =
  "ステッカー用のイラスト。ひとつだけ中央に大きく描く。フラットでかわいい画風、太めの白いふちどり、" +
  "背景は単色の薄いグレーで影なし（あとで背景を切り抜くため）。文字を入れる場合は指定された文字だけ。";

const SOURCE_IMAGE_NOTE = "添付の画像をもとに、同じ雰囲気のステッカーにする。";

/**
 * 待つ時間の上限。審査と画像を合わせても関数の制限時間（120秒。index.ts）に収め、
 * 時間切れで打ち切られて回数を戻せなくなることがないようにする。作り比べでは画像は平均3秒。
 */
const JUDGE_TIMEOUT_MS = 15_000;
const IMAGE_TIMEOUT_MS = 60_000;

/** 安全フィルターで止まったことを表す終わり方。このときは「作れませんでした」と返し、回数は戻す。 */
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
  | { kind: "safety_blocked" }
  | { kind: "no_image" };

export class GeminiHttpError extends Error {
  constructor(readonly status: number) {
    super(`Gemini API が ${status} を返した`);
  }
}

export interface GeminiClient {
  generateImage(model: string, prompt: string, sourceImage?: SourceImage): Promise<ImageResult>;
  /** 文章を審査する。答えを読めなければ undefined。 */
  judge(model: string, prompt: string): Promise<JudgeCategory | undefined>;
}

interface GenerateContentResponse {
  promptFeedback?: { blockReason?: string };
  candidates?: {
    finishReason?: string;
    content?: { parts?: { text?: string; inlineData?: { mimeType?: string; data?: string } }[] };
  }[];
}

/** 画像の生成の答えから、画像か、安全フィルターで止まったかを取り出す。 */
export function parseImageResponse(response: GenerateContentResponse): ImageResult {
  if (response.promptFeedback?.blockReason) return { kind: "safety_blocked" };
  for (const candidate of response.candidates ?? []) {
    const image = candidate.content?.parts?.find((part) => part.inlineData?.data)?.inlineData;
    if (image?.data) return { kind: "image", mimeType: image.mimeType ?? "image/png", data: image.data };
  }
  const stoppedBySafety = (response.candidates ?? []).some(
    (candidate) => candidate.finishReason && SAFETY_FINISH_REASONS.has(candidate.finishReason),
  );
  return stoppedBySafety ? { kind: "safety_blocked" } : { kind: "no_image" };
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
    if (!response.ok) throw new GeminiHttpError(response.status);
    return (await response.json()) as GenerateContentResponse;
  }

  return {
    async generateImage(model, prompt, sourceImage) {
      const instruction = STICKER_PREFIX + (sourceImage ? SOURCE_IMAGE_NOTE : "") + "\n内容：" + prompt;
      const parts: object[] = [{ text: instruction }];
      if (sourceImage) parts.unshift({ inlineData: { mimeType: sourceImage.mimeType, data: sourceImage.data } });
      const body = {
        contents: [{ role: "user", parts }],
        generationConfig: { responseModalities: ["IMAGE"], imageConfig: { aspectRatio: "1:1" } },
      };
      return parseImageResponse(await generateContent(model, body, IMAGE_TIMEOUT_MS));
    },

    async judge(model, prompt) {
      const body = {
        systemInstruction: { parts: [{ text: JUDGE_INSTRUCTION }] },
        contents: [{ role: "user", parts: [{ text: prompt }] }],
        generationConfig: { temperature: 0, responseMimeType: "application/json", responseSchema: JUDGE_RESPONSE_SCHEMA },
      };
      return parseJudgeAnswer(firstText(await generateContent(model, body, JUDGE_TIMEOUT_MS)));
    },
  };
}
