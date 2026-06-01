import { Component, Input } from '@angular/core';
@Component({ selector: 'app-stat-card', standalone: true, template: `<div class="card stat"><small>{{ label }}</small><strong>{{ value }}</strong><span>{{ hint }}</span></div>`, styles: [`.stat{display:grid;gap:.35rem}.stat small{color:#64748b;font-weight:800}.stat strong{font-size:1.65rem}.stat span{color:#64748b;font-size:.85rem}`] })
export class StatCardComponent { @Input() label=''; @Input() value: string | number = ''; @Input() hint=''; }
