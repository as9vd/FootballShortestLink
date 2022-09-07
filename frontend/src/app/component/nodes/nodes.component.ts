import { Component, Inject, Input, OnInit } from '@angular/core';
import { BfsService } from 'src/app/service/bfs/bfs.service';

@Component({
  selector: 'nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.css'],
})
export class NodesComponent implements OnInit {
  @Input() players: string[] = [];
  result: string = '';

  constructor(@Inject(BfsService) private bfsService: BfsService) {}

  ngOnInit(): void {}

  ngOnChanges(): void {
    this.bfsService.getRoute().subscribe((res: any) => {
      this.result = res;
      let response = this.result.split('->');
      for (let i = 0; i < response.length; i++) {
        this.players.push(response[i]);
      }
    });
  }
}
