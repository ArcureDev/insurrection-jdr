import { Routes } from '@angular/router';
import { MainComponent } from './main/main.component';
import { authenticationGuard } from './common/guards';

export const route = (route: string) => `/${route}`;
export const authenticatedRoute = (route: string) => `/${PATH_USER}/${route}`;
export const PATH_REGISTER = 'register';
export const PATH_LOGIN = 'login';
export const PATH_USER = 'user';
export const PATH_GAME = 'game';
export const PATH_GAME_DETAILS = 'game-details';

export const routes: Routes = [
  {
    path: '',
    redirectTo: PATH_LOGIN,
    pathMatch: 'full',
  },
  {
    path: PATH_REGISTER,
    loadComponent: () =>
      import('../app/register/register.component').then(
        (c) => c.RegisterComponent,
      ),
  },
  {
    path: PATH_LOGIN,
    loadComponent: () =>
      import('../app/login/login.component').then((c) => c.LoginComponent),
  },
  {
    path: PATH_USER,
    component: MainComponent,
    canActivate: [authenticationGuard()],
    canActivateChild: [authenticationGuard()],
    children: [
      {
        path: PATH_GAME,
        loadComponent: () =>
          import('../app/game/game.component').then((c) => c.GameComponent),
      },
      {
        path: PATH_GAME_DETAILS,
        loadComponent: () =>
          import('../app/game-details/game-details.component').then(
            (c) => c.GameDetailsComponent,
          ),
      },
    ],
  },
];
