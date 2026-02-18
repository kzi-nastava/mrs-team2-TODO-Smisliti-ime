// Custom typings to help imports in test entry files
declare module '@angular/platform-browser/fesm2022/testing.mjs' {
  export const BrowserDynamicTestingModule: any;
  export const platformBrowserDynamicTesting: any;
}

declare module '@angular/platform-browser/fesm2022/testing' {
  export const BrowserDynamicTestingModule: any;
  export const platformBrowserDynamicTesting: any;
}

declare module '@angular/platform-browser/testing' {
  export const BrowserDynamicTestingModule: any;
  export const platformBrowserDynamicTesting: any;

  export class platformBrowserTesting {
  }
}

declare const require: any;

