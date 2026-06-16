import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TokenService } from '../../core/services/token.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink],
  template: `
<div class="landing">
  <header class="nav">
    <a class="brand" routerLink="/" (click)="scrollTop($event)">
      <span class="brand-mark">O</span>
      <span>OkaneTransfer</span>
    </a>
    <nav class="nav-links">
      <a href="#features" (click)="scrollTo($event, 'features')">Solutions</a>
      <a href="#security" (click)="scrollTo($event, 'security')">Sécurité</a>
      <a href="#how-it-works" (click)="scrollTo($event, 'how-it-works')">Agences</a>
      <a href="#enterprise" (click)="scrollTo($event, 'enterprise')">Entreprise</a>
    </nav>
    <div class="nav-actions">
      <a class="btn ghost" routerLink="/auth/login">Espace Agent</a>
      <a class="btn primary" routerLink="/auth/register">Ouvrir un compte</a>
    </div>
  </header>

  <section class="hero">
    <div class="hero-badge">Nouvelle génération de transferts</div>
    <h1>Transférez de l'argent au-delà des frontières, sans limites</h1>
    <p class="hero-lead">
      La plateforme unifiant transferts nationaux et internationaux, gestion d'agences,
      conformité KYC/AML et expérience client — inspirée de Western Union &amp; MoneyGram, pensée pour le Maroc et l'Afrique.
    </p>
    <div class="hero-cta">
      <a class="btn primary lg" href="#features" (click)="scrollTo($event, 'features')">Découvrir la plateforme</a>
      <a class="btn outline lg" routerLink="/auth/login">Voir la démo live</a>
    </div>
    <div class="hero-card">
      <div class="hero-card-icon">📊</div>
      <div>
        <strong>Tableau de bord financier moderne</strong>
        <p>Transferts en temps réel, taux actualisés, gestion complète du réseau d'agences</p>
      </div>
    </div>
    <div class="hero-pills">
      <span>Sécurité bancaire</span>
      <span>Conformité KYC/AML</span>
      <span>Frais transparents</span>
      <span>Multi-devises</span>
    </div>
  </section>

  <section id="features" class="section">
    <p class="section-kicker">Fonctionnalités Premium</p>
    <h2>Conçue pour les besoins B2C et B2B modernes</h2>
    <div class="grid-4">
      <article class="feature-card">
        <h3>Transferts instantanés</h3>
        <p>Taux en temps réel, simulation des frais, corridors MA→SN, MA→FR, FR→MA et plus.</p>
      </article>
      <article class="feature-card">
        <h3>Sécurité renforcée</h3>
        <p>JWT, 2FA OTP, chiffrement AES-256 des pièces d'identité, rate limiting sur l'authentification.</p>
      </article>
      <article class="feature-card">
        <h3>Conformité intégrée</h3>
        <p>KYC documentaire, alertes AML, watchlist OFAC, tableau de bord conformité administrateur.</p>
      </article>
      <article class="feature-card">
        <h3>Gestion de réseau</h3>
        <p>Admin, managers, agents et clients — caisses, payouts, rapports PDF et journal d'audit.</p>
      </article>
    </div>
  </section>

  <section id="security" class="section alt">
    <p class="section-kicker">Architecture &amp; stack</p>
    <h2>Monolithe robuste, prêt pour la démo</h2>
    <div class="grid-3 stack">
      <div><strong>Backend</strong><span>Spring MVC 6, Spring Security, JPA, PostgreSQL, Swagger OpenAPI 3</span></div>
      <div><strong>Frontend</strong><span>Angular 20, guards, intercepteurs JWT, i18n FR/EN/AR, Chart.js</span></div>
      <div><strong>Innovation</strong><span>Chatbot IA, Mobile Money simulé, notifications email/SMS, exports PDF</span></div>
    </div>
  </section>

  <section id="how-it-works" class="section">
    <p class="section-kicker">Comment ça marche ?</p>
    <h2>3 étapes simples pour commencer</h2>
    <div class="steps">
      <article><span class="step-num">01</span><h3>Inscription &amp; KYC</h3><p>Créez un compte client, uploadez vos documents, validation par l'administrateur.</p></article>
      <article><span class="step-num">02</span><h3>Corridor &amp; simulation</h3><p>L'agent simule les frais, crée le transfert et confirme l'encaissement en agence.</p></article>
      <article><span class="step-num">03</span><h3>Code de retrait sécurisé</h3><p>Le bénéficiaire retire les fonds en agence destination après contrôle d'identité.</p></article>
    </div>
    <div class="center-cta">
      <a class="btn primary lg" routerLink="/auth/register">Ouvrir un compte</a>
    </div>
  </section>

  <section id="enterprise" class="section alt">
    <p class="section-kicker">Équipe fondatrice</p>
    <h2>Projet ENSAM — Promo 2025/2026</h2>
    <div class="team">
      <article><div class="avatar">A</div><h3>Ayman</h3><p>CEO — Vision &amp; stratégie FinTech</p></article>
      <article><div class="avatar">A</div><h3>Aghrib</h3><p>CTO — Architecture &amp; sécurité</p></article>
      <article><div class="avatar">P</div><h3>Paul</h3><p>COO — Opérations &amp; croissance</p></article>
      <article><div class="avatar">A</div><h3>Anas</h3><p>CPO — Produit &amp; design</p></article>
    </div>
  </section>

  <section id="demo" class="section demo-box">
    <h2>Prêt pour la soutenance ?</h2>
    <p>Connectez-vous avec un compte de démonstration puis explorez chaque rôle.</p>
    <div class="demo-accounts">
      <div><strong>Admin</strong> admin@okane.ma</div>
      <div><strong>Manager</strong> manager@okane.ma</div>
      <div><strong>Agent</strong> agent@okane.ma</div>
      <div><strong>Client</strong> client@okane.ma</div>
      <div class="pwd">Mot de passe : Password&#64;123</div>
    </div>
    <div class="hero-cta">
      <a class="btn primary lg" routerLink="/auth/login">Se connecter — démo live</a>
      <a class="btn outline lg" routerLink="/auth/register">Créer un compte client</a>
    </div>
  </section>

  <footer class="footer">
    <div>
      <strong>OkaneTransfer</strong>
      <p>Plateforme de gestion des transferts d'argent — cahier des charges TransferLink 2025/2026</p>
    </div>
    <div class="footer-links">
      <a routerLink="/auth/login">Connexion</a>
      <a routerLink="/auth/register">Inscription</a>
      <a href="#features" (click)="scrollTo($event, 'features')">Fonctionnalités</a>
    </div>
    <small>© 2026 OkaneTransfer — Tous droits réservés</small>
  </footer>
</div>
  `,
  styles: [`
    :host { display: block; color: #e2e8f0; }
    .landing { background: #060b14; min-height: 100vh; }
    .nav {
      position: sticky; top: 0; z-index: 50; display: flex; align-items: center; justify-content: space-between;
      gap: 1rem; flex-wrap: wrap; padding: 1rem 5vw; background: rgba(6,11,20,.85); backdrop-filter: blur(12px);
      border-bottom: 1px solid rgba(148,163,184,.15);
    }
    .brand { display: flex; align-items: center; gap: .6rem; font-weight: 800; font-size: 1.1rem; color: #fff; }
    .brand-mark {
      width: 2rem; height: 2rem; border-radius: .6rem; display: grid; place-items: center;
      background: linear-gradient(135deg, #0d9488, #14b8a6); color: #fff; font-weight: 900;
    }
    .nav-links { display: flex; gap: 1.25rem; flex-wrap: wrap; }
    .nav-links a { color: #94a3b8; font-size: .92rem; font-weight: 600; }
    .nav-links a:hover { color: #5eead4; }
    .nav-actions { display: flex; gap: .6rem; flex-wrap: wrap; }
    .btn {
      display: inline-flex; align-items: center; justify-content: center; padding: .55rem 1rem;
      border-radius: .65rem; font-weight: 700; font-size: .9rem; border: 1px solid transparent; transition: .2s;
    }
    .btn.primary { background: linear-gradient(135deg, #0d9488, #0f766e); color: #fff; }
    .btn.primary:hover { filter: brightness(1.08); }
    .btn.ghost { background: transparent; color: #e2e8f0; border-color: rgba(148,163,184,.35); }
    .btn.outline { background: transparent; color: #5eead4; border-color: #0d9488; }
    .btn.lg { padding: .8rem 1.35rem; font-size: 1rem; }
    .hero { padding: 4rem 5vw 3rem; max-width: 1100px; margin: 0 auto; }
    .hero-badge {
      display: inline-block; padding: .35rem .75rem; border-radius: 999px; font-size: .78rem; font-weight: 700;
      background: rgba(13,148,136,.2); color: #5eead4; border: 1px solid rgba(45,212,191,.3); margin-bottom: 1rem;
    }
    .hero h1 { font-size: clamp(2rem, 5vw, 3.4rem); line-height: 1.1; margin: 0 0 1rem; color: #fff; max-width: 18ch; }
    .hero-lead { color: #94a3b8; font-size: 1.1rem; line-height: 1.6; max-width: 62ch; margin: 0 0 1.75rem; }
    .hero-cta { display: flex; gap: .75rem; flex-wrap: wrap; margin-bottom: 2rem; }
    .hero-card {
      display: flex; gap: 1rem; align-items: flex-start; padding: 1.25rem; border-radius: 1rem;
      background: rgba(15,23,42,.7); border: 1px solid rgba(148,163,184,.2); max-width: 520px; margin-bottom: 1.5rem;
    }
    .hero-card-icon { font-size: 2rem; }
    .hero-card p { margin: .35rem 0 0; color: #94a3b8; font-size: .92rem; }
    .hero-pills { display: flex; flex-wrap: wrap; gap: .5rem; }
    .hero-pills span {
      padding: .35rem .7rem; border-radius: 999px; font-size: .78rem; font-weight: 700;
      background: rgba(30,41,59,.8); border: 1px solid rgba(148,163,184,.2); color: #cbd5e1;
    }
    .section { padding: 4rem 5vw; max-width: 1100px; margin: 0 auto; }
    .section.alt { background: rgba(15,23,42,.45); max-width: none; }
    .section.alt > * { max-width: 1100px; margin-left: auto; margin-right: auto; }
    .section-kicker { color: #5eead4; font-weight: 800; font-size: .85rem; text-transform: uppercase; letter-spacing: .08em; margin: 0 0 .5rem; }
    .section h2 { color: #fff; font-size: clamp(1.5rem, 3vw, 2.2rem); margin: 0 0 2rem; }
    .grid-4 { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
    .feature-card {
      padding: 1.25rem; border-radius: 1rem; background: rgba(15,23,42,.6);
      border: 1px solid rgba(148,163,184,.15);
    }
    .feature-card h3 { margin: 0 0 .5rem; color: #f8fafc; font-size: 1.05rem; }
    .feature-card p { margin: 0; color: #94a3b8; font-size: .9rem; line-height: 1.5; }
    .grid-3.stack { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem; }
    .grid-3.stack div {
      padding: 1rem; border-radius: .9rem; background: rgba(6,11,20,.5); border: 1px solid rgba(148,163,184,.12);
      display: grid; gap: .35rem;
    }
    .grid-3.stack strong { color: #f8fafc; }
    .grid-3.stack span { color: #94a3b8; font-size: .88rem; }
    .steps { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.25rem; }
    .steps article { padding: 1.25rem; border-radius: 1rem; border: 1px solid rgba(148,163,184,.15); background: rgba(15,23,42,.5); }
    .step-num { color: #5eead4; font-weight: 900; font-size: 1.5rem; }
    .steps h3 { margin: .5rem 0; color: #fff; }
    .steps p { margin: 0; color: #94a3b8; font-size: .9rem; line-height: 1.5; }
    .center-cta { text-align: center; margin-top: 2rem; }
    .team { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; }
    .team article { text-align: center; padding: 1.25rem; border-radius: 1rem; background: rgba(6,11,20,.5); border: 1px solid rgba(148,163,184,.12); }
    .avatar {
      width: 3rem; height: 3rem; margin: 0 auto .75rem; border-radius: 999px; display: grid; place-items: center;
      background: linear-gradient(135deg, #0d9488, #115e59); font-weight: 900; color: #fff;
    }
    .team h3 { margin: 0; color: #fff; }
    .team p { margin: .35rem 0 0; color: #94a3b8; font-size: .85rem; }
    .demo-box { text-align: center; }
    .demo-box p { color: #94a3b8; max-width: 50ch; margin: 0 auto 1.5rem; }
    .demo-accounts {
      display: flex; flex-wrap: wrap; justify-content: center; gap: .75rem 1.5rem; margin-bottom: 1.5rem;
      padding: 1rem; border-radius: 1rem; background: rgba(13,148,136,.12); border: 1px solid rgba(45,212,191,.25);
    }
    .demo-accounts div { color: #cbd5e1; font-size: .9rem; }
    .demo-accounts .pwd { width: 100%; color: #5eead4; font-weight: 800; }
    .footer {
      padding: 2rem 5vw 3rem; border-top: 1px solid rgba(148,163,184,.15);
      display: flex; flex-wrap: wrap; gap: 1.5rem; justify-content: space-between; align-items: flex-end;
      max-width: 1100px; margin: 0 auto; color: #64748b; font-size: .88rem;
    }
    .footer strong { color: #e2e8f0; display: block; margin-bottom: .35rem; }
    .footer-links { display: flex; gap: 1rem; flex-wrap: wrap; }
    .footer-links a { color: #94a3b8; }
    .footer-links a:hover { color: #5eead4; }
    @media (max-width: 768px) {
      .nav-links { display: none; }
      .hero { padding-top: 2.5rem; }
    }
  `]
})
export class LandingComponent implements OnInit {
  private readonly tokens = inject(TokenService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    const user = this.tokens.getUser();
    if (user && this.tokens.isAuthenticated()) {
      this.auth.redirectByRole(user);
    }
  }

  scrollTo(event: Event, id: string): void {
    event.preventDefault();
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  scrollTop(event: Event): void {
    event.preventDefault();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
