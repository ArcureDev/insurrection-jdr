import { Component, inject, resource } from '@angular/core';
import { HttpService } from './http.service';
import { RouterOutlet } from '@angular/router';

type Toto = {
  name: string;
};

@Component({
  selector: 'ins-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  private readonly httpService = inject(HttpService);

  toto() {
    const r = resource<Toto, void>({
      loader: async ({ request }) => {
        return this.httpService.sweetFetch('api/users');
      },
    });
    const a = r.value();
  }
}
