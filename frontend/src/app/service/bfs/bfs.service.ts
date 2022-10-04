import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class BfsService {
  private baseUrl = 'http://localhost:8080';
  public footballer1 = '';
  public footballer2 = '';

  constructor(@Inject(HttpClient) private httpClient: HttpClient) {}

  setFootballerOne(input: string) {
    this.footballer1 = input;
  }

  setFootballerTwo(input: string) {
    this.footballer2 = input;
  }

  getRoute() {
    return this.httpClient.get(
      this.baseUrl + '/' + this.footballer1 + '+' + this.footballer2,
      { responseType: 'text' }
    );
  }
}
