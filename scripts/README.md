# Scripts dev — OkaneTransfer

Pour tester la branche `frontend-api-on-finance-V2` en local.

## 1. Récupérer la branche

```bash
git fetch origin
git checkout frontend-api-on-finance-V2
```

## 2. Base PostgreSQL

```powershell
.\scripts\init-db.ps1
```

Puis appliquer (une fois) :

- `schema.sql`
- `seed_users.sql`

Les agences, corridors et grilles de frais sont créés automatiquement au démarrage du backend (`ReferentialSeedService`) si la table `agencies` est vide.

Comptes de test : `admin@okane.ma` / `Password@123`

## 3. Config backend

Dans `src/main/resources/application.properties`, remplir au minimum :

```
aes.key=OkaneTransferDevAesKey1234567890
jwt.secret=OkaneTransferDevJwtSecretKey1234567890
```

Ou charger les variables avec `.\scripts\set-dev-env.ps1` avant de lancer Tomcat.

## 4. Lancer et tester

- Backend : Tomcat / IntelliJ Smart Tomcat (context path : `/okane_transfer_war`)
- Frontend : `npm install` puis `npm start -- --port 4300`
- Smoke test :

```cmd
scripts\test-api.cmd http://localhost:8080/okane_transfer_war
```
