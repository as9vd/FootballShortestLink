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
  showLoader = true;

  constructor(@Inject(BfsService) private bfsService: BfsService) {}

  ngOnInit(): void {
    this.players = [];
  }

  ngOnChanges(): void {
    this.showLoader = true;
    console.log('Initial: ' + this.showLoader);

    this.bfsService.getRoute().subscribe(
      (res: any) => {
        this.result = res;
        let response = this.result.split('->');
        for (let i = 0; i < response.length; i++) {
          this.players.push(response[i]); // Get the route from the API and push it into the players list.
          // Then, it'll display.
        }
        this.showLoader = false;
        console.log('After: ' + this.showLoader);
      },
      (error) => (this.showLoader = false)
    );

    console.log(
      this.bfsService.footballer1 + ' vs. ' + this.bfsService.footballer2
    );

    // console.log('Loading? ' + this.showLoader);
  }
}
