import { Component, Inject, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { map, Observable } from 'rxjs';
import { FootballerService } from 'src/app/service/footballer/footballer.service';
import { BfsService } from 'src/app/service/bfs/bfs.service';
import { startWith } from 'rxjs/operators';
import * as footballers from '../../../assets/footballerDatabase.json';

@Component({
  selector: 'searchbar',
  templateUrl: './searchbar.component.html',
  styleUrls: ['./searchbar.component.css'],
})
export class SearchbarComponent implements OnInit {
  keyword = 'name';
  contactForm!: FormGroup;
  data: string[] = [];
  player1: string = 'RyanSEPGiggs';
  player2: string = 'BrunoSEPFernandes';
  result: string = '';

  nodes: string[] = [];
  loading = true;

  constructor(
    @Inject(FootballerService) private footballerService: FootballerService,
    @Inject(BfsService) private bfsService: BfsService
  ) {}

  ngOnInit() {
    this.loading = true;

    this.footballerService.getFootballerList().subscribe((res: any) => {
      res.forEach((element: any) => {
        this.data.push(element.name + ' ' + element.birthday);
      });
      this.data.sort((one, two) => (two > one ? -1 : 1));
    });

    this.bfsService.setFootballerOne('Ryan Giggs');
    this.bfsService.setFootballerTwo(this.player2);

    this.bfsService.getRoute().subscribe((res: any) => {
      this.result = res;
    });

    this.player2 = '';

    let response = this.result.split('->');
    for (let i = 0; i < response.length; i++) {
      this.nodes.push(response[i]);
    }

    this.loading = false;
  }

  onChange($event: Event, deviceValue: string) {
    this.loading = true;
    this.result = '';
    this.nodes = [];
    let parts = deviceValue.split(' (');

    for (let i = 0; i < parts.length; i++) {
      let curr = deviceValue.split(' ')[i];
      if (curr.includes('(')) break;

      if (i == parts.length - 1) {
        this.player2 += curr;
      } else {
        this.player2 += curr + 'SEP';
      }
    }

    this.bfsService.setFootballerOne('Ryan Giggs');
    this.bfsService.setFootballerTwo(this.player2);

    this.bfsService.getRoute().subscribe((res: any) => {
      this.result = res;
    });

    this.player2 = '';

    let response = this.result.split('->');
    for (let i = 0; i < response.length; i++) {
      this.nodes.push(response[i]);
    }

    this.nodes = this.nodes.filter(function (e: any) {
      return e;
    });

    this.loading = false;
  }
}
