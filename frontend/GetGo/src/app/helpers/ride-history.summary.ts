import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class RideHistorySummaryHelper {

  getRideSummary(address: string): string {
    if (!address) return '';

    const parts = address.split(',').map(p => p.trim());

    const firstPart = parts[0] || '';
    const secondPart = parts[1] || '';

    let cityOrMunicipality = parts.find(p => p.startsWith('Град '));

    if (!cityOrMunicipality) {
      cityOrMunicipality = parts.find(p => p.startsWith('Општина '));
    }

    const name = cityOrMunicipality
      ? cityOrMunicipality.replace(/^Град |^Општина /, '')
      : '';

    return `${firstPart}, ${secondPart}${name ? ', ' + name : ''}`;
  }
}

