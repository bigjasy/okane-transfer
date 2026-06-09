import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
@Component({ selector: 'app-secured-layout', standalone: true, imports: [RouterOutlet, NavbarComponent, SidebarComponent], template: `<div class="shell"><app-sidebar /><main><app-navbar /><section class="content"><router-outlet /></section></main></div>`, styles: [`.shell{display:grid;grid-template-columns:280px 1fr;min-height:100vh}main{min-width:0}.content{padding:1.25rem}@media(max-width:900px){.shell{grid-template-columns:1fr}.content{padding:.9rem}}`] })
export class SecuredLayoutComponent {}
