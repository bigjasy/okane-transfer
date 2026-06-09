import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
@Component({ selector: 'app-public-layout', standalone: true, imports: [RouterOutlet], template: `<main class="public"><router-outlet /></main>`, styles: [`.public{min-height:100vh;display:grid;place-items:center;background:linear-gradient(135deg,#0f172a,#1d4ed8);padding:1rem}`] })
export class PublicLayoutComponent {}
