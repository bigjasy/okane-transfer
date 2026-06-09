import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
@Component({ selector: 'app-data-table', standalone: true, imports: [CommonModule], template: `<div class="table-wrap"><table><thead><tr>@for(c of columns; track c){<th>{{c}}</th>}</tr></thead><tbody><ng-content /></tbody></table></div>` })
export class DataTableComponent { @Input() columns: string[] = []; }
