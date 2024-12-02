import {
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  Injector,
  Resource,
  resource,
  viewChild,
} from '@angular/core';
import { ButtonComponent } from '../../atomic-design/button/button.component';
import { DefaultComponent } from '../abstract-default.component';
import { api, apiWithParams } from '../http.service';
import { Game } from '../types';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import {
  authenticatedRoute,
  PATH_GAME,
  PATH_LOGIN,
  route,
} from '../app.routes';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { notBlankValidator } from '../utils/validator.utils';
import { InputComponent } from '../../atomic-design/input/input.component';

@Component({
  selector: 'ins-main',
  imports: [
    ButtonComponent,
    RouterLink,
    ReactiveFormsModule,
    InputComponent,
    RouterOutlet,
  ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss',
})
export class MainComponent extends DefaultComponent {
  createGameDialog =
    viewChild<ElementRef<HTMLDialogElement>>('createGameDialog');
  joinGameDialog = viewChild<ElementRef<HTMLDialogElement>>('joinGameDialog');

  private readonly router = inject(Router);
  private readonly formBuilder = inject(FormBuilder);
  private readonly injector = inject(Injector);

  createNewGameResource?: Resource<Game | null>;
  createGameForm = this.formBuilder.nonNullable.group({
    name: this.formBuilder.nonNullable.control<string>('', [
      Validators.required,
      notBlankValidator,
    ]),
  });
  joinGameForm = this.formBuilder.nonNullable.group({
    id: this.formBuilder.nonNullable.control<string>('', [
      Validators.required,
      notBlankValidator,
    ]),
    name: this.formBuilder.nonNullable.control<string>('', [
      Validators.required,
      notBlankValidator,
    ]),
  });

  currentGame = computed(() => this.httpService.currentGame());

  protected readonly route = route;
  protected readonly PATH_GAME = PATH_GAME;
  protected readonly authenticatedRoute = authenticatedRoute;

  constructor() {
    super();

    effect(() => {
      if (!this.createNewGameResource?.value()) return;
    });
  }

  createGame() {
    if (this.createGameForm.invalid) {
      return;
    }
    if (!this.createNewGameResource) {
      this.createNewGameResource = resource({
        loader: async () => {
          return this.httpService
            .sweetFetch<
              Game,
              string
            >(api('games'), 'POST', this.createGameForm.controls.name.value)
            .then((game) => {
              this.toto(game, this.createGameDialog());
              return game;
            });
        },
        injector: this.injector,
      });
      return;
    }
    this.createNewGameResource.reload();
  }

  joinGame() {
    if (this.joinGameForm.invalid) {
      return;
    }
    resource({
      loader: async () => {
        return this.httpService
          .sweetFetch<
            Game,
            string
          >(apiWithParams('games', { id: this.joinGameForm.value.id }), 'PUT', this.joinGameForm.controls.name.value)
          .then((game) => {
            this.toto(game, this.joinGameDialog());
          });
      },
      injector: this.injector,
    });
  }

  logout() {
    this.httpService.logout().then(() => {
      this.router.navigate([route(PATH_LOGIN)]);
    });
  }

  private toto(game: Game, dialogElementRef?: ElementRef<HTMLDialogElement>) {
    dialogElementRef?.nativeElement.close();
    this.httpService.subscribeToGameUpdates();
    this.router.navigate([authenticatedRoute(PATH_GAME)]);
    this.httpService.currentGame.set(game);
  }
}
