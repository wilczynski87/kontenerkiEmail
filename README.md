# kontenerkiEmail

Serwis Ktor do wysyłki faktur i rachunków e-mailem (Gmail API) dla systemu magazynków kontenerowych.

## Wymagania

- JDK 21
- Plik `libs/library-1.0.0.jar` — wspólna biblioteka modeli (`Invoice`, `Path`, itd.). Umieść JAR w katalogu `libs/` przed buildem.

## Zmienne środowiskowe

| Zmienna | Opis |
|---------|------|
| `ENV` | `DEV` lub `PROD` — w DEV maile do klientów trafiają na adres testowy |
| `EMAIL_USER` | Adres nadawcy (konto Gmail) |
| `GOOGLE_CLIENT_ID` | OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | OAuth2 client secret |
| `GOOGLE_REFRESH_TOKEN` | Refresh token Gmail (seed przy pierwszym uruchomieniu) |
| `GOOGLE_REFRESH_TOKEN_FILE` | Plik persystencji refresh tokena (domyślnie `data/google-refresh.token`) |
| `API_NAME` | Host wewnętrznego API |
| `API_PORT` | Port wewnętrznego API |
| `INTERNAL_API_KEY` | Klucz `X-Internal-Key` do API |
| `PRINT_RECIPIENT` | Odbiorca maili „fakturki do druku” (domyślnie `wilczynski87@gmail.com`) |
| `EMAIL_PORT` | Port serwera (domyślnie `8200`) |

## Endpointy

- `GET /healthcheck` — status
- `POST /sendMailWithAttachment/withVat` — faktura z VAT (kolejka)
- `POST /sendMailWithAttachment/noVat` — rachunek bez VAT (kolejka)
- `POST /sendMailWithAttachment/sendInvoiceAgain` — ponowna wysyłka
- `POST /printInvoices` — zbiorczy PDF do druku (Gmail API)

## Uruchomienie

```bash
./gradlew run
```

```bash
./gradlew test
./gradlew build
```

Docker: obraz na porcie **8200**, start `bin/email`. Zamontuj wolumen na `GOOGLE_REFRESH_TOKEN_FILE`, żeby rotacja refresh tokena przetrwała restart.

## OAuth (`com.kontenery.oauth`)

Pakiet automatycznie odświeża access token Gmail i zapisuje nowy refresh token (gdy Google go zwróci):

- `AutoRefreshTokenProvider` — cache + odświeżanie w tle
- `FileRefreshTokenStore` — zapis refresh tokena na dysk
- `GoogleTokenRefresher` — wywołanie `oauth2.googleapis.com/token`
