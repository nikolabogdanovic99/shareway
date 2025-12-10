<script>
  import { onMount, onDestroy } from 'svelte';
  import { browser } from '$app/environment';

  let { startLocation = '', endLocation = '', pickupLocations = [] } = $props();

  let mapContainer;
  let map;
  let L;

  // Geocode address to coordinates using Nominatim (free)
  async function geocode(address) {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&limit=1`,
        {
          headers: {
            'User-Agent': 'ShareWay-App'
          }
        }
      );
      const data = await response.json();
      if (data && data.length > 0) {
        return {
          lat: parseFloat(data[0].lat),
          lng: parseFloat(data[0].lon),
          displayName: data[0].display_name
        };
      }
    } catch (error) {
      console.error('Geocoding error:', error);
    }
    return null;
  }

  // Get route between multiple points using OSRM (free)
  async function getRouteMultiple(waypoints) {
    if (waypoints.length < 2) return null;
    
    try {
      // Build coordinates string: lng,lat;lng,lat;...
      const coordsString = waypoints.map(p => `${p.lng},${p.lat}`).join(';');
      
      const response = await fetch(
        `https://router.project-osrm.org/route/v1/driving/${coordsString}?overview=full&geometries=geojson`
      );
      const data = await response.json();
      if (data.routes && data.routes.length > 0) {
        return data.routes[0].geometry.coordinates;
      }
    } catch (error) {
      console.error('Routing error:', error);
    }
    return null;
  }

  async function initMap() {
    if (!browser || !mapContainer) return;

    // Dynamically import Leaflet (client-side only)
    L = await import('leaflet');

    // Initialize map centered on Switzerland
    map = L.default.map(mapContainer).setView([46.8, 8.2], 8);

    // Add OpenStreetMap tiles
    L.default.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Custom icons
    const startIcon = L.default.divIcon({
      className: 'custom-marker',
      html: '<div style="background-color: #28a745; color: white; width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; border: 2px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">A</div>',
      iconSize: [30, 30],
      iconAnchor: [15, 15]
    });

    const endIcon = L.default.divIcon({
      className: 'custom-marker',
      html: '<div style="background-color: #dc3545; color: white; width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; border: 2px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">B</div>',
      iconSize: [30, 30],
      iconAnchor: [15, 15]
    });

    // Pickup icon (yellow)
    const createPickupIcon = (index) => L.default.divIcon({
      className: 'custom-marker',
      html: `<div style="background-color: #ffc107; color: black; width: 26px; height: 26px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; border: 2px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3); font-size: 12px;">${index + 1}</div>`,
      iconSize: [26, 26],
      iconAnchor: [13, 13]
    });

    // Geocode start and end
    const [startCoords, endCoords] = await Promise.all([
      geocode(startLocation + ', Switzerland'),
      geocode(endLocation + ', Switzerland')
    ]);

    const bounds = [];
    const routeWaypoints = [];

    // Add start marker
    if (startCoords) {
      L.default.marker([startCoords.lat, startCoords.lng], { icon: startIcon })
        .addTo(map)
        .bindPopup(`<strong>Start:</strong><br>${startLocation}`);
      bounds.push([startCoords.lat, startCoords.lng]);
      routeWaypoints.push(startCoords);
    }

    // Geocode and add pickup markers
    if (pickupLocations && pickupLocations.length > 0) {
      for (let i = 0; i < pickupLocations.length; i++) {
        const pickup = pickupLocations[i];
        const pickupCoords = await geocode(pickup.location + ', Switzerland');
        if (pickupCoords) {
          L.default.marker([pickupCoords.lat, pickupCoords.lng], { icon: createPickupIcon(i) })
            .addTo(map)
            .bindPopup(`<strong>Pickup ${i + 1}:</strong><br>${pickup.location}<br><small>${pickup.riderName || 'Rider'}</small>`);
          bounds.push([pickupCoords.lat, pickupCoords.lng]);
          routeWaypoints.push(pickupCoords);
        }
      }
    }

    // Add end marker
    if (endCoords) {
      L.default.marker([endCoords.lat, endCoords.lng], { icon: endIcon })
        .addTo(map)
        .bindPopup(`<strong>Destination:</strong><br>${endLocation}`);
      bounds.push([endCoords.lat, endCoords.lng]);
      routeWaypoints.push(endCoords);
    }

    // Get and draw route through all waypoints
    if (routeWaypoints.length >= 2) {
      const routeCoords = await getRouteMultiple(routeWaypoints);
      if (routeCoords) {
        // OSRM returns [lng, lat], Leaflet needs [lat, lng]
        const latLngs = routeCoords.map(coord => [coord[1], coord[0]]);
        L.default.polyline(latLngs, {
          color: '#007bff',
          weight: 4,
          opacity: 0.7
        }).addTo(map);
      } else {
        // Fallback: straight lines through all points
        const fallbackPoints = routeWaypoints.map(p => [p.lat, p.lng]);
        L.default.polyline(fallbackPoints, {
          color: '#007bff',
          weight: 3,
          opacity: 0.5,
          dashArray: '10, 10'
        }).addTo(map);
      }
    }

    // Fit map to show all markers
    if (bounds.length > 0) {
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }

  onMount(() => {
    initMap();
  });

  onDestroy(() => {
    if (map) {
      map.remove();
    }
  });
</script>

<svelte:head>
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
</svelte:head>

<div bind:this={mapContainer} class="map-container"></div>

<style>
  .map-container {
    width: 100%;
    height: 300px;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #ddd;
  }
</style>