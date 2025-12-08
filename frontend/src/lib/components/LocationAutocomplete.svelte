<script>
  let { 
    id = '', 
    name = '', 
    placeholder = '', 
    value = $bindable(''),
    required = false 
  } = $props();

  let suggestions = $state([]);
  let showSuggestions = $state(false);
  let isLoading = $state(false);
  let selectedIndex = $state(-1);
  let isValidSelection = $state(false);  // NEW: tracks if user selected from list
  let inputElement;
  let debounceTimer;

  // Search for locations using Nominatim
  async function searchLocations(query) {
    if (query.length < 2) {
      suggestions = [];
      return;
    }

    isLoading = true;
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&countrycodes=ch&limit=5&addressdetails=1`,
        {
          headers: {
            'User-Agent': 'ShareWay-App'
          }
        }
      );
      const data = await response.json();
      
      suggestions = data.map(item => ({
        displayName: formatDisplayName(item),
        fullName: item.display_name,
        lat: item.lat,
        lon: item.lon
      }));
    } catch (error) {
      console.error('Search error:', error);
      suggestions = [];
    }
    isLoading = false;
  }

  // Format the display name to be shorter and cleaner
  function formatDisplayName(item) {
    const parts = [];
    
    if (item.address) {
      if (item.address.railway) {
        parts.push(item.address.railway);
      } else if (item.address.aeroway) {
        parts.push(item.address.aeroway);
      } else if (item.address.building) {
        parts.push(item.address.building);
      } else if (item.address.road) {
        parts.push(item.address.road);
      }
      
      const city = item.address.city || item.address.town || item.address.village || item.address.municipality;
      if (city && !parts.includes(city)) {
        parts.push(city);
      }
      
      if (item.address.state) {
        parts.push(item.address.state);
      }
    }
    
    if (parts.length === 0) {
      return item.display_name.split(',').slice(0, 2).join(', ');
    }
    
    return parts.join(', ');
  }

  // Handle input change with debounce
  function handleInput(event) {
    value = event.target.value;
    isValidSelection = false;  // Reset when user types
    selectedIndex = -1;
    
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      searchLocations(value);
      showSuggestions = true;
    }, 300);
  }

  // Handle suggestion selection
  function selectSuggestion(suggestion) {
    value = suggestion.displayName;
    isValidSelection = true;  // Mark as valid selection
    suggestions = [];
    showSuggestions = false;
    selectedIndex = -1;
  }

  // Handle keyboard navigation
  function handleKeydown(event) {
    if (!showSuggestions || suggestions.length === 0) return;

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        selectedIndex = Math.min(selectedIndex + 1, suggestions.length - 1);
        break;
      case 'ArrowUp':
        event.preventDefault();
        selectedIndex = Math.max(selectedIndex - 1, -1);
        break;
      case 'Enter':
        if (selectedIndex >= 0) {
          event.preventDefault();
          selectSuggestion(suggestions[selectedIndex]);
        }
        break;
      case 'Escape':
        showSuggestions = false;
        selectedIndex = -1;
        break;
    }
  }

  // Handle blur - clear if not valid selection
  function handleBlur() {
    setTimeout(() => {
      showSuggestions = false;
      selectedIndex = -1;
      
      // If user typed but didn't select from list, clear the input
      if (!isValidSelection && value) {
        value = '';
      }
    }, 200);
  }

  // Handle focus
  function handleFocus() {
    if (suggestions.length > 0) {
      showSuggestions = true;
    }
  }
</script>

<div class="autocomplete-wrapper">
  <div class="input-wrapper">
    <input
      bind:this={inputElement}
      type="text"
      class="form-control"
      class:is-valid={isValidSelection && value}
      {id}
      {name}
      {placeholder}
      {required}
      {value}
      oninput={handleInput}
      onkeydown={handleKeydown}
      onblur={handleBlur}
      onfocus={handleFocus}
      autocomplete="off"
    />
    {#if isValidSelection && value}
      <span class="valid-icon">✓</span>
    {/if}
  </div>
  
  {#if isLoading}
    <div class="autocomplete-loading">
      <small class="text-muted">Searching...</small>
    </div>
  {/if}

  {#if showSuggestions && suggestions.length > 0}
    <ul class="autocomplete-suggestions">
      {#each suggestions as suggestion, index}
        <li 
          class="autocomplete-item"
          class:selected={index === selectedIndex}
          onmousedown={() => selectSuggestion(suggestion)}
        >
          <span class="suggestion-icon">📍</span>
          <span class="suggestion-text">{suggestion.displayName}</span>
        </li>
      {/each}
    </ul>
  {/if}

  {#if !isLoading && !showSuggestions && value && !isValidSelection}
    <small class="text-danger">Please select a location from the list</small>
  {/if}
</div>

<style>
  .autocomplete-wrapper {
    position: relative;
  }

  .input-wrapper {
    position: relative;
  }

  .valid-icon {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    color: #198754;
    font-weight: bold;
  }

  .form-control.is-valid {
    border-color: #198754;
    padding-right: 2.5rem;
  }

  .autocomplete-loading {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
  }

  .autocomplete-suggestions {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: white;
    border: 1px solid #ddd;
    border-top: none;
    border-radius: 0 0 8px 8px;
    list-style: none;
    margin: 0;
    padding: 0;
    z-index: 1000;
    max-height: 250px;
    overflow-y: auto;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  }

  .autocomplete-item {
    padding: 10px 12px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    border-bottom: 1px solid #eee;
  }

  .autocomplete-item:last-child {
    border-bottom: none;
  }

  .autocomplete-item:hover,
  .autocomplete-item.selected {
    background-color: #f0f7ff;
  }

  .suggestion-icon {
    flex-shrink: 0;
  }

  .suggestion-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
</style>