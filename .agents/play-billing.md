# 回数券の購入（Play Billing）

AI 素材の回数券（#348）を Google Play で買う仕組み。アプリは Play で買い、購入トークンをサーバー（`verifyPurchase`）に送る。サーバーが Play に購入を確かめて回数を足し、消費する。サーバー側の決まりは `functions/README.md` の「verifyPurchase」。

- **コードの場所**:
    - `data/infra/PlayBillingRemoteSource.kt`：Play Billing Library を呼ぶのはここだけ。アイテム ID `TICKET_PRODUCT_ID` もここ。
    - `data/repository/TicketPurchaseRepository.kt`：Play で買い、サーバーに送る。購入と送り直しは同時に走らせない。
    - `edit/aimaterial/AiMaterialViewModel.kt`：購入の案内（AI 素材の画面の中のカード）の状態と計測。

- **決まり**:
    - アプリは消費しない（`consumeAsync` を呼ばない）。消費はサーバーが回数を足したあとに行います。アプリが消費すると、回数を足す前に通信が切れたとき、払った人に回数が渡りません。
    - 値段はコードに書かず、Play から取って出します（公開後に Play の価格実験で変えるため）。回数（5回）と期間（6か月）はサーバーと同じ値をアプリにも書いています（`AiMaterialUiState.kt`）。変えるときはサーバーとアプリの両方を直し、アプリを出し直してください。出回っている古い版は前の値を出し続けます。
    - 足せていない購入（コンビニ払いがあとで済んだ・足す前に通信が切れた）は、AI 素材の画面に来た・戻ったときと、購入の案内を開いたときにサーバーへ送り直します。
    - Crashlytics には、端末・アカウント・通信の都合で起きる Play のエラー（`PlayBillingException.isCausedByDeviceOrNetwork`）とサーバーの断りを残しません。それ以外の Play のエラー（アイテムが見つからない・呼び方の誤りなど）は設定や実装の誤りなので残します。

- **動かすための設定（コードの外）**:
    1. Play Console のデベロッパー アカウントにお支払いプロファイル（販売者アカウント）をつなぐ。ないとアプリ内アイテムを売れません。
    2. Play Billing Library の入ったビルド（`com.android.vending.BILLING` の権限が入る）をテスト版のトラックに上げてから、アプリ内アイテムを作る。作り方は `functions/README.md` の「本番に出す」の 8 です。
    3. 購入を試す Google アカウントを、Play Console の「設定 > ライセンス テスト」に登録する。登録した人はテスト用のお支払い方法で買えて、請求されません。「数分後に承認される」テストカードを選ぶと、支払い待ち（コンビニ払いと同じ流れ）を試せます。
    4. debug と beta も applicationId は `com.fansauchiwa` なので、ライセンス テスターなら Play の購入を試せます（試せないときは、Play のヘルプ「Google Play Billing Library の統合をテストする」で条件を確かめてください）。購入の案内に「今は回数券を買えません」と出たら、2 のアイテムが有効か、端末の Google アカウントが 3 に入っているかを確かめてください。

- **Play Billing Library の更新**:
    - Play は Billing Library のメジャー版ごとに、アプリの更新を受け付けなくなる期限を決めています。期限が近づくと Play Console に警告が出ます。
    - 上げるときは `gradle/libs.versions.toml` の `billing` を変え、ユニットテストと `assembleBeta`（R8）まで通してください（`.agents/r8.md`）。

- **持ち主が変わるとき**:
    - アプリを別の Play Console アカウントへ移すときは、移し先にも 1 のお支払いプロファイルが要ります。手数料の段階の登録はデベロッパー アカウントごとなので、移し先で確かめてください。条件は Play Console のヘルプ「アプリを別のデベロッパー アカウントに移行する」で確かめてください。
    - サーバーが Play に購入を確かめる権限（`functions/README.md` の「本番に出す」の招待）もやり直してください。しないと購入の確認がすべて `verification_failed` になります。
    - 購入の案内からリンクしている特定商取引法に基づく表記（`AiMaterialScreen.kt` の `COMMERCIAL_TRANSACTIONS_URL`）の販売者の情報を、新しい持ち主のものに書き換えてください。リンク先は Firebase Hosting の既定のドメインなので、Firebase プロジェクトを替えたら URL も直してください。
