// class="format-price"を持つ要素(金額が書かれている要素)をすべてHTMLから取得
const priceElements = document.querySelectorAll(".format-price");

// 取得した要素を1件ずつ処理する
priceElements.forEach(function (element) {
  // 要素からテキスト内容を取得する
  const elementText = element.textContent;
  // formatPrice関数を呼び出して、返却値をHTMLに設定する
  element.textContent = formatPrice(elementText);
});

// formatPrice関数
// 数値をカンマ区切りの金額形式に変換する
// 例) 100000 →  ¥ 100,000
function formatPrice(elementText) {
  // 要素のテキスト内容を数値に変換する
  const price = parseInt(elementText);
  // 数値に変換されたかを確認
  if (Number.isInteger(price)) {
    // カンマ区切り(3桁区切り)に変換し、¥ 記号を付けて返却する
    return "¥ " + price.toLocaleString();
  }
  // 数値に変換できなければ ¥ 0を返却する
  return "¥ 0";
}
