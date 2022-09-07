import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class FootballerService {
  constructor(private http: HttpClient) {}

  getFootballerList() {
    let headers = new HttpHeaders();
    headers.set('Content-Type', 'application/json');

    return this.http.get<JSON[]>('../../../assets/footballerDatabase.json', {
      headers,
    });
  }
}
