import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { map, Observable } from 'rxjs';
import { FootballerService } from 'src/app/service/footballer.service';
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

  constructor(private footballerService: FootballerService) {}

  ngOnInit() {
    this.footballerService.getFootballerList().subscribe((res: any) => {
      res.forEach((element: any) => {
        this.data.push(element.name + ' ' + element.birthday);
      });
      this.data.sort((one, two) => (two > one ? -1 : 1));
    });
  }

  submit() {
    console.log(this.data);
  }
}
