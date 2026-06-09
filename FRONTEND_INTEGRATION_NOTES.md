# OkaneTransfer — Frontend Angular généré

Ce frontend a été généré pour respecter la documentation technique OkaneTransfer et la structure backend Spring MVC/Tomcat existante.

## Démarrage frontend

```bash
cd okaneTransfer
rm -rf node_modules package-lock.json
npm config set registry https://registry.npmjs.org/
npm install
npm start -- --port 4300
```

Ouvrir : `http://localhost:4300/auth/login`

## Configuration API

Fichier : `src/environments/environment.ts`

```ts
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1',
  useMockApi: false,
  allowMockFallback: true
};
```

- `useMockApi: false` : le frontend tente d’appeler le backend réel.
- `allowMockFallback: true` : si un endpoint n’existe pas encore côté backend, l’interface continue avec des données mock pour éviter de bloquer la démonstration.
- Pour une intégration stricte finale, passer `allowMockFallback` à `false`.

## Comptes mock utiles

Si `useMockApi` est remis à `true` :

- `admin@okane.ma / Password@123`
- `manager@okane.ma / Password@123`
- `agent@okane.ma / Password@123`
- `client@okane.ma / Password@123`

## Endpoints backend détectés

Présents dans le backend uploadé :

- `/api/v1/auth/register-client`
- `/api/v1/auth/login`
- `/api/v1/auth/verify-otp`
- `/api/v1/auth/refresh`
- `/api/v1/auth/logout`
- `/api/v1/auth/logout-all`
- `/api/v1/auth/me`
- `/api/v1/otp/request`, `/verify`, `/enable-2fa`, `/disable-2fa`
- `/api/v1/users`
- `/api/v1/countries`
- `/api/v1/currencies`
- `/api/v1/exchange-rates`
- `/api/v1/agencies`
- `/api/v1/corridors`
- `/api/v1/transfers`
- `/api/v1/transfers/{reference}/confirm-payment`
- `/api/v1/payouts/confirm`
- `/api/v1/cash-registers/open`
- `/api/v1/cash-registers/{id}/close`
- `/api/v1/aml/alerts`
- `/api/v1/aml/watchlist`
- `/api/v1/kyc/documents`
- `/api/v1/notifications/me`

## Points backend manquants ou incomplets à compléter

Le PDF prévoit plus de fonctionnalités que le backend actuel. Ces éléments sont actuellement mockés côté frontend :

1. Dashboards
   - `GET /api/v1/dashboards/admin`
   - `GET /api/v1/dashboards/manager`
   - `GET /api/v1/dashboards/agent`
   - `GET /api/v1/dashboards/client`

2. Grilles de frais
   - Contrôleur `/api/v1/fee-grids` absent.
   - Simulation officielle `/api/v1/fee-grids/simulate` ou `/api/v1/transfers/simulate` non détectée.

3. Transferts
   - `GET /api/v1/transfers/track/{reference}` non détecté.
   - `PATCH /api/v1/transfers/{reference}/cancel` non détecté.
   - `POST /api/v1/transfers/simulate` non détecté.

4. Payout
   - `POST /api/v1/payouts/search` non détecté.
   - `POST /api/v1/payouts/validate` non détecté.
   - Seul `POST /api/v1/payouts/confirm` est présent.

5. Cash register
   - `GET /api/v1/cash-registers/current?currencyCode=MAD` non détecté.
   - `GET/POST /api/v1/cash-registers/{id}/movements` non détecté.
   - Seuls `open` et `close` sont présents.

6. Bénéficiaires
   - Contrôleur `/api/v1/beneficiaries` non détecté.

7. Mobile Money
   - Contrôleur `/api/v1/mobile-money` non détecté.

8. Chatbot
   - Contrôleur `/api/v1/chatbot` non détecté.

9. Rapports et audit global
   - `/api/v1/reports/*` absent.
   - `/api/v1/audit-logs` global absent. Il existe seulement l’audit par utilisateur via `/users/{id}/audit-logs`.

## Modifications backend incluses

`CorsConfig.java` autorise maintenant :

- `http://localhost:4200`
- `http://localhost:4300`
- `http://127.0.0.1:4200`
- `http://127.0.0.1:4300`

Cela permet de lancer Angular sur 4200 ou 4300 sans erreur CORS.
