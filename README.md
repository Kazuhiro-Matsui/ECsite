# 仮想 EC サイト：Spring Boot 3 + MyBatis + Thymeleaf による Web アプリ実装デモ

> **TL;DR** — Java + Spring Boot 3.5 で実装した**仮想 EC サイトのプロトタイプ**。
> 商品一覧 → 商品詳細 → 注文 → 注文完了画面までの**MVC + DB 連携の最小フロー**を、
> MyBatis（SQL マッパー） + MySQL + Thymeleaf（サーバーサイドテンプレート）で構築。
> Web アプリ開発で**最も基礎的な「3 層アーキテクチャ（Controller / DAO / View）」**を自分の手で動かし、
> 機械学習プロジェクトに加えて**Web 系バックエンドの実装力**もあることを示すための作品。

---

## 📌 この成果物で示せること（採用担当者向け要約）

| 観点              | 内容                                                                                  |
| ----------------- | ------------------------------------------------------------------------------------- |
| **位置付け**      | 約 1 年前に作成した Web アプリ実装デモ（チュートリアル教材を参考に独自実装）        |
| **ドメイン**      | EC サイト（商品閲覧 → 注文登録 → 履歴記録）                                            |
| **アーキテクチャ**| **MVC + 3 層構造**（Controller / DAO / DTO / View）の基礎を踏襲                       |
| **技術スタック**  | Java 17 / **Spring Boot 3.5** / **MyBatis** / Thymeleaf / MySQL / Gradle              |
| **学んだこと**    | DI（`@Autowired`）、`@Mapper` による SQL マッパー、`ModelAndView`、Thymeleaf テンプレート構文 |

---

## 1. 背景：なぜこの課題を選んだか

- Web アプリの**リクエスト → コントローラー → DB → ビュー**という基本フローを自分の手で組み立てる
- **Spring Boot のエコシステム**（DI、AutoConfiguration、Starter）に慣れる
- **SQL を直接書く**（MyBatis）スタイルで DB アクセス層を理解する

ことを目的に、最も題材として定番の「商品一覧 → 注文」フローを持つ EC サイトを実装した。


---

## 2. 機能仕様

仮想 EC サイトとして、最低限の購買フローを 3 画面で実装している。

| URL                              | 画面              | 機能                                                                 |
| -------------------------------- | ----------------- | -------------------------------------------------------------------- |
| `/ecsite/product`                | 商品一覧画面      | DB から全商品（カテゴリ JOIN 済み）を取得し、サムネイル付きで一覧表示 |
| `/ecsite/detail?productId={id}`  | 商品詳細画面      | 商品 ID で 1 件取得し、商品説明・価格・購入数量入力を表示             |
| `/ecsite/order?productId={id}…`  | 注文登録 → 完了    | `order_history` テーブルへ INSERT し、注文完了画面を表示              |

### 取り扱い商品（イメージ）
椅子 (chair1〜3.png) / 観葉植物 (plant1〜3.png) / 電球 (light_bulb1〜3.png) の計 9 商品を想定。
画像は `src/main/resources/static/images/` に静的配置。

---

## 3. アーキテクチャ：なぜこの構成を選んだか

### 3.1 技術選定ロジック

| レイヤー         | 採用技術               | 理由                                                                  |
| ---------------- | ---------------------- | --------------------------------------------------------------------- |
| **言語**         | Java 17                | LTS かつ Spring Boot 3 系の前提バージョン                              |
| **アプリ FW**    | **Spring Boot 3.5.5**  | DI / AutoConfiguration / Starter による開発スピードと業務実績の豊富さ |
| **永続化**       | **MyBatis** 3.0.5      | **SQL を自分で書く透明性**を重視（JPA より学習段階で挙動が見えやすい） |
| **ビュー**       | **Thymeleaf**          | サーバーサイドレンダリング、Spring 公式推奨                            |
| **DB**           | MySQL 8.0              | デファクトスタンダード                                                |
| **ビルド**       | Gradle (Wrapper 同梱)  | `gradlew` で環境依存を最小化                                          |
| **ホットリロード** | Spring Boot DevTools  | 開発中の再起動コスト削減                                              |

### 3.2 レイヤー構成（3 層アーキテクチャ）

```
[ブラウザ]
   │ HTTP
   ▼
┌──────────────────────────┐
│  Controller 層            │  ProductController, OrderHistoryController
│  ・URL ルーティング        │  (@Controller + @GetMapping)
│  ・リクエスト/レスポンス    │  ModelAndView でビュー名とデータを返却
└────────┬─────────────────┘
         │ @Autowired
         ▼
┌──────────────────────────┐
│  DAO 層 (MyBatis Mapper)  │  ProductDao, OrderHistoryDao
│  ・SQL を直接記述          │  (@Mapper + @Select / @Insert)
│  ・結果を DTO にマッピング │  map-underscore-to-camel-case=true
└────────┬─────────────────┘
         │ JDBC
         ▼
┌──────────────────────────┐
│  MySQL                    │  product, category, order_history テーブル
└──────────────────────────┘

[DTO]  ProductDto …… 商品情報を表す Plain Java Object
[View] Thymeleaf …… product_list.html / detail.html / complete.html
```

### 3.3 実装ハイライト

#### MyBatis の `@Select` で SQL を Java コードに同居
```java
@Select("""
    select p.product_id, p.product_name, p.product_price,
           p.product_image_path, c.category_name
    from product AS p
    inner join category AS c on p.category_id = c.category_id
    order by p.product_id
""")
public ArrayList<ProductDto> getAllProducts();
```
- Java 17 の **テキストブロック (`"""..."""`)** を活用して SQL を可読に
- `map-underscore-to-camel-case=true` の設定により `product_name` → `productName` を自動変換

#### Thymeleaf の `th:each` で商品一覧をループ描画
```html
<div th:each="product : ${productList}" class="product">
  <a th:href="@{/ecsite/detail(productId=${product.productId})}">
    <img th:src="@{${product.productImagePath}}">
  </a>
  <div th:text="${product.categoryName}" class="common-tag"></div>
  <div th:text="${product.productName}"  class="label"></div>
  <div th:text="${product.productPrice}" class="format-price"></div>
</div>
```

#### 注文登録のフロー（INSERT → 直近の `ordered_at` を再取得して完了画面へ）
```java
orderHistoryDao.insertOrderHistory(productId, orderCount);
LocalDateTime orderdAt = orderHistoryDao.getLatestOrderedAt(productId);
mav.addObject("orderedAt", orderdAt);
mav.setViewName("complete");
```

---

## 4. データベース構造（推定）

`application.properties` の接続先は `ec_practice` データベース。テーブル定義は以下のとおり。

### `product` テーブル
| カラム                | 型 (推定)    | 内容           |
| --------------------- | ------------ | -------------- |
| `product_id`          | INT (PK)     | 商品 ID        |
| `product_name`        | VARCHAR      | 商品名         |
| `product_description` | TEXT         | 商品説明       |
| `product_price`       | INT          | 価格 (円)      |
| `product_image_path`  | VARCHAR      | 画像の相対パス |
| `category_id`         | INT (FK)     | カテゴリ ID    |

### `category` テーブル
| カラム          | 型 (推定) | 内容          |
| --------------- | --------- | ------------- |
| `category_id`   | INT (PK)  | カテゴリ ID   |
| `category_name` | VARCHAR   | カテゴリ名    |

### `order_history` テーブル
| カラム        | 型 (推定)   | 内容           |
| ------------- | ----------- | -------------- |
| `product_id`  | INT (FK)    | 商品 ID        |
| `order_count` | INT         | 注文数         |
| `ordered_at`  | DATETIME    | 注文日時 (now()) |

> ℹ️ テーブル作成 DDL は本リポジトリには同梱されていないため、実行時には `ec_practice` データベースを手動で作成し、上記スキーマに従って各テーブルを用意する必要があります（→ 改善案として後述）。

---

## 5. 学んだこと

| 学習項目                              | 具体的に身についたこと                                                          |
| ------------------------------------- | ------------------------------------------------------------------------------- |
| **Spring Boot の DI コンテナ**         | `@Autowired` でインターフェースに実装を注入する流れ                              |
| **MVC アーキテクチャ**                | `@Controller` / `@GetMapping` / `ModelAndView` を使ったリクエスト処理            |
| **MyBatis（SQL マッパー）**           | `@Mapper` インターフェースに `@Select` / `@Insert` を書くだけで DB アクセス可能 |
| **Thymeleaf テンプレート**            | `th:each` / `th:text` / `th:href="@{...}"` などサーバーサイド描画の構文          |
| **Gradle Wrapper**                    | `./gradlew bootRun` で環境差を吸収してビルド実行                                 |
| **静的リソース管理**                  | `src/main/resources/static/` 配下の CSS / JS / 画像の配信                       |
| **CSS の責務分離**                    | `common/reset.css`, `variable.css`, `base.css`, `header.css` ... の分割管理     |

---

## 6. 限界とリファクタ余地（正直なふりかえり）

このプロジェクトはチュートリアル相当の最小実装にとどまっており、本格運用にはほど遠い。意図的に小さく作っているが、改善するなら以下が必要。

### 6.1 セキュリティ
| 課題                                            | 改善案                                                              |
| ----------------------------------------------- | ------------------------------------------------------------------- |
| **DB パスワードが `application.properties` に平文** | **環境変数化** (`${DB_PASSWORD}`) + `.env` を `.gitignore` で除外    |
| `/ecsite/order` が **GET メソッド** で副作用あり    | **POST に変更**して CSRF トークン検証を入れる                       |
| 認証・認可がない                                  | Spring Security による セッション + パスワードハッシュ化導入        |
| 入力値バリデーションがない                        | `orderCount` の負値・上限チェック (`@Validated` + Bean Validation) |

### 6.2 アーキテクチャ
- **Service 層が欠如**：Controller が直接 DAO を呼んでいるためビジネスロジックが置き場がない
- **トランザクション境界がない**：注文 INSERT と在庫減算が同時に必要になった場合に `@Transactional` が必要
- **DTO と Entity の分離が曖昧**：表示専用 DTO とドメイン Entity を分ける必要が出てくる

### 6.3 機能
- **カート機能なし** → セッションスコープの Bean か Redis でカート保持
- **複数商品の同時注文不可**
- **在庫管理なし** → product テーブルに stock カラム追加 + 注文時に減算
- **会員登録・ログインなし**
- **検索・絞り込みなし**

### 6.4 開発プロセス
- **DDL ファイル (`schema.sql` / `data.sql`) を同梱していない** → 再現性のため `src/main/resources/` に追加すべき
- **テストが `EcSiteApplicationTests.java` 1 個（空の起動テスト）のみ** → Controller / DAO の単体テストを追加
- **Docker Compose 化されていない** → MySQL を docker-compose で立てる構成にすれば README の手順が大幅に短くなる
- **誤って `Cardiovascular_Disease.ipynb` がコミットされている** → 別プロジェクトのファイル、削除すべき

---

## 7. 次に作るならどうするか

このプロジェクトを今の知識で発展させるなら以下を導入する:

- **Spring Security** による認証・セッション管理
- **Service 層**を挟んだ責務分離と `@Transactional` 制御
- **REST API 化** + フロントを **React / Vue.js** に分離（SPA + バックエンド API）
- **Docker Compose** で MySQL + アプリを 1 コマンド起動
- **CI/CD**（GitHub Actions で `./gradlew test` 自動実行）
- **Flyway / Liquibase** による DB マイグレーション管理）

---

## 📁 ファイル構成

```
ECsite/
├── build.gradle                    # Gradle ビルド設定
├── settings.gradle
├── gradlew / gradlew.bat            # Gradle Wrapper
├── gradle/wrapper/
└── src/
    ├── main/
    │   ├── java/com/example/ec_site/
    │   │   ├── SbEcsiteApplication.java   # エントリポイント
    │   │   ├── controller/
    │   │   │   ├── ProductController.java       # 商品一覧・詳細
    │   │   │   └── OrderHistoryController.java  # 注文登録
    │   │   ├── dao/
    │   │   │   ├── ProductDao.java              # 商品 SQL マッパー
    │   │   │   └── OrderHistoryDao.java         # 注文履歴 SQL マッパー
    │   │   └── dto/
    │   │       └── ProductDto.java              # 商品 DTO
    │   └── resources/
    │       ├── application.properties           # DB 接続設定
    │       ├── static/
    │       │   ├── css/{common/, complete.css, detail.css, product_list.css}
    │       │   ├── js/formatPrice.js
    │       │   └── images/{chair, plant, light_bulb}*.png
    │       └── templates/
    │           ├── product_list.html
    │           ├── detail.html
    │           └── complete.html
    └── test/java/com/example/ec_site/
        └── EcSiteApplicationTests.java          # 空の起動テストのみ
```

---

## 🚀 実行方法

### 前提環境
- Java 17 (`java -version` で確認)
- MySQL 8.0 系
- Git

### 1. リポジトリをクローン
```bash
git clone https://github.com/Kazuhiro-Matsui/ECsite.git
cd ECsite
```

### 2. MySQL の準備

ローカルの MySQL に `ec_practice` データベースを作成。

```sql
CREATE DATABASE ec_practice CHARACTER SET utf8mb4;
USE ec_practice;

-- 例：カテゴリ
CREATE TABLE category (
  category_id   INT PRIMARY KEY,
  category_name VARCHAR(50) NOT NULL
);

-- 例：商品
CREATE TABLE product (
  product_id          INT PRIMARY KEY,
  product_name        VARCHAR(100) NOT NULL,
  product_description TEXT,
  product_price       INT NOT NULL,
  product_image_path  VARCHAR(255),
  category_id         INT,
  FOREIGN KEY (category_id) REFERENCES category(category_id)
);

-- 例：注文履歴
CREATE TABLE order_history (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  product_id   INT NOT NULL,
  order_count  INT NOT NULL,
  ordered_at   DATETIME NOT NULL,
  FOREIGN KEY (product_id) REFERENCES product(product_id)
);
```

> 上記は Java/SQL コードからの**推定スキーマ**です。商品データのサンプル INSERT 文も併せて投入してください（画像は `/images/chair1.png` のような相対パスで `product_image_path` カラムに保存）。

### 3. DB 接続設定

`src/main/resources/application.properties` を編集してローカル環境の MySQL 接続情報を設定します。

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ec_practice?serverTimezone=Asia/Tokyo
spring.datasource.username=YOUR_USER
spring.datasource.password=YOUR_PASSWORD
```

> ⚠️ **セキュリティ注意**：本リポジトリの `application.properties` には開発時のパスワードがそのまま残っています。実運用では **環境変数化** (`spring.datasource.password=${DB_PASSWORD}`) し、リポジトリにパスワードを含めないでください。

### 4. アプリケーション起動

```bash
# macOS / Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

ブラウザで <http://localhost:8080/ecsite/product> にアクセスして商品一覧画面が表示されれば成功です。

### 5. （オプション）ビルド成果物として実行

```bash
./gradlew build
java -jar build/libs/ec_site-0.0.1-SNAPSHOT.jar
```

---

## 👤 Author

**Kazuhiro Matsui**
GitHub: [@Kazuhiro-Matsui](https://github.com/Kazuhiro-Matsui)
