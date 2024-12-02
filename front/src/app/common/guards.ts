import { inject } from '@angular/core';
import { CanActivateChildFn, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';

export const authenticationGuard = (): CanActivateChildFn => {
  return ():
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree => {
    const router = inject(Router);

    // const expiration = localStorage.getItem('expiration');
    // if (!expiration || new Date() > new Date(JSON.parse(expiration))) {
    //   return authService.getAuthenticatedUser().pipe(
    //     map(authenticatedUser => {
    //       if (!authenticatedUser) {
    //         localStorage.clear();
    //         authService.isAuthenticated.set(false);
    //         return router.parseUrl('/home');
    //       }
    //       authService.setConnectedUserInfo(authenticatedUser);
    //       authService.isAuthenticated.set(true);
    //       return true;
    //     })
    //   );
    // }
    // authService.isAuthenticated.set(true);
    return true;
  };
};
