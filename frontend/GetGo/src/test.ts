// Provide Node-like globals for libraries that expect them in the browser test environment
;(globalThis as any).global = (globalThis as any).global || (window as any);
;(globalThis as any).process = (globalThis as any).process || { env: {} };

import 'zone.js';
import 'zone.js/testing';

import { getTestBed } from '@angular/core/testing';

// Require platform-browser-dynamic testing at runtime to avoid static ESM export issues
const pbTesting: any = require('@angular/platform-browser-dynamic/testing');

getTestBed().initTestEnvironment(pbTesting.BrowserDynamicTestingModule, pbTesting.platformBrowserDynamicTesting());

// Prevent Karma from running tests until Angular test environment is set up
declare const require: any;

// Load only the register spec
require('./app/pages/authentication/register/register.spec.ts');
