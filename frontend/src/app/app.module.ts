import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { importProvidersFrom } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { routes } from './app.routes';
import { App } from './app';

@NgModule({
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    App
  ],
  providers: [
    provideRouter(routes) // use standalone routes
  ],
  bootstrap: [App] // bootstrap not needed here for standalone apps
})
export class AppModule { }
