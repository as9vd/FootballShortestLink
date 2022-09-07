import { TestBed } from '@angular/core/testing';

import { FootballerService } from './footballer.service';

describe('FootballerService', () => {
  let service: FootballerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FootballerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
