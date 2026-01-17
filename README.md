```markdown
# JavaFX Sample (Maven) — Barcode Printing (with UPC-A & GS1-128 support)

This sample project demonstrates generating and printing barcodes with ZXing and JavaFX. It now includes:

- CODE_128
- UPC-A (raw 11-digit payload)
- UPC-A (embedded weight)
- GS1-128 (GTIN + weight using AI 01 and AI 310n)

GS1-128 (GTIN + weight) notes
- The app builds a GS1-128 payload by prefixing the data with the FNC1 control (the code sends the FNC1 character '\u00f1' to ZXing), followed by:
  - AI 01 + GTIN (14 digits, left-padded if necessary)
  - AI 310n + 6-digit weight field, where n (0–6) indicates the number of implied decimal places and the weight field is 6 digits with leading zeros
- Example:
  - GTIN: 0123456789012 (will be left-padded to 14 digits -> 00123456789012)
  - Weight: 1.234 kg, decimals = 3 -> scaled weight = 1234 -> field = 001234
  - Encoded data sent to ZXing will be: FNC1 + "01" + "00123456789012" + "3103" + "001234"
- AI 310n semantics: the weight is represented in kilograms and n defines the implied number of decimals. The numeric payload for the AI must be exactly 6 digits (leading zeros allowed).
- Because AI 01 and AI 310n are fixed-length fields, no group separators are required between them.

Usage
- Select "GS1-128 (GTIN + weight)" from the barcode type selector.
- Enter:
  - GTIN (1–14 digits; will be left-padded to 14),
  - Weight in kilograms (decimal allowed, non-negative),
  - Decimals (0–6) to set the implied decimal places for AI 310n.
- Click Generate to preview the GS1-128 barcode (encoded as CODE-128 with FNC1).
- Click Print Barcode to open the print dialog and send it to a printer or PDF printer.

Technical details & caveats
- ZXing: GS1-128 is represented as a CODE-128 symbol with a leading FNC1. We pass the FNC1 character '\u00f1' as the first character of the encoded string; ZXing will encode it correctly as the FNC1 function code.
- The sample currently builds only AI 01 (GTIN) + AI 310n (net weight). If you need additional AIs or variable-length fields, the Group Separator (ASCII 29, '\u001D') should be used between variable-length fields; the code can be extended to insert it where appropriate.
- Validate fields carefully before using printed labels in production workflows; GS1 and retailer requirements vary.
- If you require strict GTIN check-digit calculation or support for other GS1 AIs (like expiration date AI 17, lot AI 10, variable-length AIs), I can extend the UI and encoding to cover those.

Run
- Same as before (Maven + javafx-maven-plugin). Ensure you set -Djavafx.platform to match your OS.

Next steps (optional)
- Add GTIN check-digit computation and validation.
- Add support for additional GS1 AIs (expiry date, lot, serial).
- Allow inserting Group Separator when adding variable-length AIs.
- Provide presets for common label printers / label sizes.
```