import { AfterViewInit, Component, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-chart-card',
  standalone: true,
  template: `<div class="card"><h3>{{ title }}</h3><div class="canvas-wrap"><canvas #canvas></canvas></div></div>`,
  styles: [`h3{margin:0 0 1rem}.canvas-wrap{height:230px}`]
})
export class ChartCardComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() title = 'Graphique';
  @Input() type: ChartType = 'line';
  @Input() data: ChartConfiguration['data'] = { labels: [], datasets: [] };
  @Input() options: ChartConfiguration['options'] = { responsive: true, maintainAspectRatio: false };
  @ViewChild('canvas') canvas?: ElementRef<HTMLCanvasElement>;
  private chart?: Chart;

  ngAfterViewInit(): void { this.render(); }
  ngOnChanges(_: SimpleChanges): void { this.render(); }
  ngOnDestroy(): void { this.chart?.destroy(); }
  private render(): void {
    if (!this.canvas) return;
    this.chart?.destroy();
    this.chart = new Chart(this.canvas.nativeElement, { type: this.type, data: this.data, options: this.options });
  }
}
