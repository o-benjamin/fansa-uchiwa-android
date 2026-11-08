const API_BASE = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications";

/** Play の答えを待つ時間の上限。購入の確認は利用者が画面で待っているので、短めにする。 */
const TIMEOUT_MS = 15_000;

/** `ProductPurchase.purchaseState` の値 */
export const PurchaseState = { PURCHASED: 0, CANCELED: 1, PENDING: 2 } as const;
/** `ProductPurchase.consumptionState` の値 */
export const ConsumptionState = { NOT_CONSUMED: 0, CONSUMED: 1 } as const;

/**
 * Play Developer API の `purchases.products.get` の答えのうち、使うところ。
 * 数値の項目は、既定値（0）のとき答えから省かれることがあるので、ないときは 0 として読む。
 */
export interface ProductPurchase {
  /** `PurchaseState`（購入済み・取り消し・支払い待ち（コンビニ払いなど）） */
  purchaseState?: number;
  /** `ConsumptionState`（まだ消費していない・消費済み） */
  consumptionState?: number;
  /** Play の注文番号（`GPA.` で始まる）。問い合わせで利用者が見せるのはこの番号 */
  orderId?: string;
  /** 購入した時刻（ミリ秒。API は文字列で返す） */
  purchaseTimeMillis?: string;
  /** 数量。Play Console で数量の選択を有効にしない（functions/README.md の「本番に出す」）ので、付かないか1 */
  quantity?: number;
}

/** Voided Purchases API の答えの1件（返金・取り消しされた購入）。 */
export interface VoidedPurchase {
  purchaseToken: string;
  orderId?: string;
  voidedTimeMillis?: string;
  /** 0：利用者、1：デベロッパー、2：Google */
  voidedSource?: number;
  /** 返金の理由。値の意味は Play のドキュメントの voidedReason（例：1 は気が変わった、5 は不正、7 はチャージバック） */
  voidedReason?: number;
}

export interface VoidedPurchasesPage {
  voidedPurchases: VoidedPurchase[];
  nextPageToken?: string;
}

/**
 * Play Developer API がエラーを返した。`apiStatus` / `apiMessage` は答えの `error.status` / `error.message`
 * （権限がない・トークンが違う、などを見分ける。購入トークンは入らない）。
 */
export class PlayHttpError extends Error {
  constructor(
    readonly status: number,
    readonly apiStatus?: string,
    readonly apiMessage?: string,
  ) {
    super(`Play Developer API が ${status}（${apiStatus ?? "理由なし"}）を返した: ${apiMessage ?? ""}`);
  }
}

export interface PlayBillingClient {
  getProductPurchase(productId: string, purchaseToken: string): Promise<ProductPurchase>;
  /** 消費済みにする（同じアイテムをまた買えるようにする。購入の承認も兼ねる） */
  consumeProductPurchase(productId: string, purchaseToken: string): Promise<void>;
  /** 返金・取り消しされたアプリ内アイテムの購入（過去30日分。API の既定）を1ページ分返す */
  listVoidedPurchases(pageToken?: string): Promise<VoidedPurchasesPage>;
}

async function toHttpError(response: Response): Promise<PlayHttpError> {
  const body = (await response.json().catch(() => undefined)) as { error?: { status?: string; message?: string } } | undefined;
  return new PlayHttpError(response.status, body?.error?.status, body?.error?.message);
}

/**
 * Play Developer API を REST で呼ぶ。`getAccessToken` は androidpublisher のスコープのトークンを返す（index.ts）。
 * `fetchImpl` はテストで差し替える。
 */
export function createPlayBillingClient(
  packageName: string,
  getAccessToken: () => Promise<string>,
  fetchImpl: typeof fetch = fetch,
): PlayBillingClient {
  const appBase = `${API_BASE}/${encodeURIComponent(packageName)}/purchases`;

  async function call(url: string, method: "GET" | "POST"): Promise<Response> {
    const response = await fetchImpl(url, {
      method,
      headers: { authorization: `Bearer ${await getAccessToken()}` },
      signal: AbortSignal.timeout(TIMEOUT_MS),
    });
    if (!response.ok) throw await toHttpError(response);
    return response;
  }

  function productUrl(productId: string, purchaseToken: string): string {
    return `${appBase}/products/${encodeURIComponent(productId)}/tokens/${encodeURIComponent(purchaseToken)}`;
  }

  return {
    async getProductPurchase(productId, purchaseToken) {
      return (await (await call(productUrl(productId, purchaseToken), "GET")).json()) as ProductPurchase;
    },

    async consumeProductPurchase(productId, purchaseToken) {
      await call(`${productUrl(productId, purchaseToken)}:consume`, "POST");
    },

    async listVoidedPurchases(pageToken) {
      const query = pageToken ? `?token=${encodeURIComponent(pageToken)}` : "";
      const body = (await (await call(`${appBase}/voidedpurchases${query}`, "GET")).json()) as {
        voidedPurchases?: VoidedPurchase[];
        tokenPagination?: { nextPageToken?: string };
      };
      return { voidedPurchases: body.voidedPurchases ?? [], nextPageToken: body.tokenPagination?.nextPageToken };
    },
  };
}
