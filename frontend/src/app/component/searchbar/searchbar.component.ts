import { Component, Inject, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { map, Observable } from 'rxjs';
import { FootballerService } from 'src/app/service/footballer/footballer.service';
import { BfsService } from 'src/app/service/bfs/bfs.service';
import { startWith } from 'rxjs/operators';
import * as footballers from '../../../assets/FootballerGraphFormatted.json';

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

  constructor(
    @Inject(FootballerService) private footballerService: FootballerService,
    @Inject(BfsService) private bfsService: BfsService
  ) {}

  // When we initialise the component, get the footballers from the API and put them into the list.
  ngOnInit() {
    this.footballerService.getFootballerList().subscribe((res: any) => {
      res.forEach((element: any) => {
        this.data.push(element.name + ' (' + element.birthday + ')');
      });
      this.data.sort((one, two) => (two > one ? -1 : 1)); // Sort alphabetically.
    });

    this.bfsService.setFootballerOne('Ryan Giggs'); // Giggsy will always be the first footballer.
    this.bfsService.setFootballerTwo(this.player2); // This is whoever is picked; initially, it's Bruno Fernandes.

    this.bfsService.getRoute().subscribe((res: any) => {
      this.result = res; // Gets the route from the API and sets it to the class's variable.
    });

    this.player2 = '';

    let response = this.result.split('->'); // Breaking up the return string into something that can be displayed properly.
    for (let i = 0; i < response.length; i++) {
      this.nodes.push(response[i]);
    }
  }

  onChange($event: Event, deviceValue: string) {
    this.result = '';
    this.nodes = [];
    let parts = deviceValue.split(' ('); // Getting the name from the string.

    for (let i = 0; i < parts.length; i++) {
      let curr = deviceValue.split(' ')[i]; // Name.
      if (curr.includes('(')) break; // Then it's faulty.

      if (i == parts.length - 1) {
        this.player2 += curr; // If it's a singular name (e.g. Deco, Alisson, Derlei).
      } else {
        this.player2 += curr + 'SEP'; // Otherwise.
      }
    }

    this.bfsService.setFootballerOne('Ryan Giggs'); // Bit redundant this, but for sanity sake.
    this.bfsService.setFootballerTwo(this.player2); // The newly selected player.

    this.bfsService.getRoute().subscribe((res: any) => {
      this.result = res; // Again, get the route from the API, and update the class's variable.
    });

    this.player2 = ''; // Reset.

    let response = this.result.split('->');
    for (let i = 0; i < response.length; i++) {
      this.nodes.push(response[i]);
    }

    this.nodes = this.nodes.filter(function (e: any) {
      return e;
    });
  }
}
