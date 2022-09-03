import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent } from './app.component';
import { SearchbarComponent } from './component/searchbar/searchbar.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatAutocompleteModule } from '@angular/material/autocomplete';

@NgModule({
  declarations: [AppComponent, SearchbarComponent],
  imports: [BrowserModule, NoopAnimationsModule, MatAutocompleteModule],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
