import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent } from './app.component';
import { SearchbarComponent } from './component/searchbar/searchbar.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { FootballerService } from './service/footballer.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AutocompleteLibModule } from 'angular-ng-autocomplete';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAutocompleteModule } from '@angular/material/autocomplete';

@NgModule({
  declarations: [AppComponent, SearchbarComponent],
  imports: [
    BrowserModule,
    NoopAnimationsModule,
    AutocompleteLibModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    MatNativeDateModule,
    MatSelectModule,
    MatFormFieldModule,
    MatAutocompleteModule,
  ],
  providers: [FootballerService],
  bootstrap: [AppComponent],
})
export class AppModule {}
