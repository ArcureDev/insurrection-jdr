import { Component, inject } from '@angular/core';
import { HttpService } from './http.service';

@Component({
  selector: '',
  imports: [],
  template: '',
})
export abstract class DefaultComponent {
  protected readonly httpService = inject(HttpService);
}
