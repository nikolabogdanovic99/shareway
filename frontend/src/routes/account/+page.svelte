<script>
  import { enhance } from "$app/forms";

  let { data, form } = $props();
  let { user, isAuthenticated, dbUser } = data;

  let firstName = $state(dbUser?.firstName || "");
  let lastName = $state(dbUser?.lastName || "");
  let phoneNumber = $state(dbUser?.phoneNumber || "");
  let profileImage = $state(dbUser?.profileImage || "");
  let profileImagePreview = $state(dbUser?.profileImage || "");

  let licenseImageFront = $state(dbUser?.licenseImageFront || "");
  let licenseImageBack = $state(dbUser?.licenseImageBack || "");
  let licenseFrontPreview = $state(dbUser?.licenseImageFront || "");
  let licenseBackPreview = $state(dbUser?.licenseImageBack || "");

  // Edit modes
  let isEditingProfile = $state(false);
  let isEditingVerification = $state(false);

  // Update when data changes
  $effect(() => {
    if (data.dbUser) {
      firstName = data.dbUser.firstName || "";
      lastName = data.dbUser.lastName || "";
      phoneNumber = data.dbUser.phoneNumber || "";
      profileImage = data.dbUser.profileImage || "";
      profileImagePreview = data.dbUser.profileImage || "";
      licenseImageFront = data.dbUser.licenseImageFront || "";
      licenseImageBack = data.dbUser.licenseImageBack || "";
      licenseFrontPreview = data.dbUser.licenseImageFront || "";
      licenseBackPreview = data.dbUser.licenseImageBack || "";
    }
    // Close edit mode after successful save
    if (form?.success) {
      isEditingProfile = false;
      isEditingVerification = false;
    }
  });

  // Check if user is admin
  const isAdmin = $derived(user?.user_roles?.includes("admin"));

  // Handle profile image upload
  function handleProfileImageUpload(event) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        profileImage = e.target.result;
        profileImagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  // Clear profile image
  function clearProfileImage() {
    profileImage = "";
    profileImagePreview = "";
  }

  // Handle license front upload
  function handleLicenseFrontUpload(event) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        licenseImageFront = e.target.result;
        licenseFrontPreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  // Handle license back upload
  function handleLicenseBackUpload(event) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        licenseImageBack = e.target.result;
        licenseBackPreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  // Clear license front
  function clearLicenseFront() {
    licenseImageFront = "";
    licenseFrontPreview = "";
  }

  // Clear license back
  function clearLicenseBack() {
    licenseImageBack = "";
    licenseBackPreview = "";
  }

  // Cancel editing - reset to original values
  function cancelProfileEdit() {
    firstName = dbUser?.firstName || "";
    lastName = dbUser?.lastName || "";
    phoneNumber = dbUser?.phoneNumber || "";
    profileImage = dbUser?.profileImage || "";
    profileImagePreview = dbUser?.profileImage || "";
    isEditingProfile = false;
  }

  function cancelVerificationEdit() {
    licenseImageFront = dbUser?.licenseImageFront || "";
    licenseImageBack = dbUser?.licenseImageBack || "";
    licenseFrontPreview = dbUser?.licenseImageFront || "";
    licenseBackPreview = dbUser?.licenseImageBack || "";
    isEditingVerification = false;
  }

  // Get status badge class
  function getStatusClass(status) {
    switch (status) {
      case "VERIFIED":
        return "bg-success";
      case "PENDING":
        return "bg-warning text-dark";
      case "DENIED":
        return "bg-danger";
      default:
        return "bg-secondary";
    }
  }

  // Check states
  const isProfileComplete = $derived(firstName && lastName);
  const canRequestVerification = $derived(
    isProfileComplete && licenseImageFront && licenseImageBack,
  );
  const canSaveProfile = $derived(firstName.trim() && lastName.trim());
</script>

<h1 class="mt-3">Account Details</h1>

{#if isAuthenticated}
  {#if form?.success && form?.action === "profile"}
    <div class="alert alert-success">Profile updated successfully!</div>
  {/if}

  {#if form?.success && form?.action === "verification"}
    <div class="alert alert-success">
      Verification request submitted! An admin will review it soon.
    </div>
  {/if}

  {#if form?.error}
    <div class="alert alert-danger">{form.error}</div>
  {/if}

  <!-- Account Info Card -->
  <div class="card mb-4">
    <div class="card-header">
      <h5 class="mb-0">Account Info</h5>
    </div>
    <div class="card-body">
      <div class="d-flex align-items-center">
        <img
          src={user.picture}
          alt="Profile"
          class="rounded-circle me-3"
          width="80"
        />
        <div>
          <p class="mb-1"><strong>Email:</strong> {user.email}</p>
          {#if user.user_roles && user.user_roles.length > 0}
            <p class="mb-1">
              <strong>Role:</strong>
              {user.user_roles.join(", ")}
            </p>
          {/if}
          {#if isAdmin}
            <span class="badge bg-primary"
              >Admin - No verification required</span
            >
          {/if}
        </div>
      </div>
    </div>
  </div>

  <!-- Profile Card -->
  <div class="card mb-4">
    <div class="card-header d-flex justify-content-between align-items-center">
      <h5 class="mb-0">Profile</h5>
      <div>
        {#if isProfileComplete}
          <span class="badge bg-success me-2">Complete</span>
        {:else}
          <span class="badge bg-warning text-dark me-2">Incomplete</span>
        {/if}
        {#if !isEditingProfile}
          <button
            class="btn btn-sm btn-outline-primary"
            onclick={() => (isEditingProfile = true)}
          >
            Edit
          </button>
        {/if}
      </div>
    </div>
    <div class="card-body">
      {#if !isEditingProfile}
        <!-- View Mode -->
        <div class="d-flex align-items-center">
          <div class="me-4">
            {#if profileImagePreview}
              <img
                src={profileImagePreview}
                alt="Profile"
                class="rounded-circle"
                style="width: 100px; height: 100px; object-fit: cover;"
              />
            {:else}
              <div
                class="bg-secondary rounded-circle d-flex align-items-center justify-content-center"
                style="width: 100px; height: 100px;"
              >
                <span class="text-white fs-4"
                  >{firstName?.charAt(0) || "?"}{lastName?.charAt(0) ||
                    ""}</span
                >
              </div>
            {/if}
          </div>
          <div>
            <p class="mb-1"><strong>First Name:</strong> {firstName || "-"}</p>
            <p class="mb-1"><strong>Last Name:</strong> {lastName || "-"}</p>
            <p class="mb-1">
              <strong>Phone:</strong>
              {phoneNumber ? "+41 " + phoneNumber : "-"}
            </p>
          </div>
        </div>
        {#if !isProfileComplete}
          <div class="alert alert-info mt-3 mb-0">
            Complete your profile to book rides. Click "Edit" to add your name.
          </div>
        {/if}
      {:else}
        <!-- Edit Mode -->
        <form method="POST" action="?/updateProfile" use:enhance>
          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label" for="firstName">First Name *</label>
              <input
                class="form-control"
                id="firstName"
                name="firstName"
                type="text"
                bind:value={firstName}
              />
            </div>
            <div class="col-md-6">
              <label class="form-label" for="lastName">Last Name *</label>
              <input
                class="form-control"
                id="lastName"
                name="lastName"
                type="text"
                bind:value={lastName}
              />
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label" for="phoneNumber"
              >Phone Number (optional)</label
            >
            <div class="input-group">
              <span class="input-group-text">+41</span>
              <input
                class="form-control"
                id="phoneNumber"
                name="phoneNumber"
                type="tel"
                placeholder="79 123 45 67"
                maxlength="12"
                bind:value={phoneNumber}
                oninput={(e) => {
                  // Nur Zahlen und Leerzeichen erlauben
                  e.target.value = e.target.value.replace(/[^0-9\s]/g, "");
                  phoneNumber = e.target.value;
                }}
              />
            </div>
            <small class="text-muted"
              >9 Ziffern nach +41 (z.B. 79 123 45 67)</small
            >
          </div>

          <div class="mb-3">
            <label class="form-label" for="profileImageUpload"
              >Profile Image (optional)</label
            >
            <input
              class="form-control"
              id="profileImageUpload"
              type="file"
              accept="image/*"
              onchange={handleProfileImageUpload}
            />
            <input type="hidden" name="profileImage" value={profileImage} />
          </div>

          {#if profileImagePreview}
            <div class="mb-3">
              <img
                src={profileImagePreview}
                alt="Profile Preview"
                class="rounded"
                style="max-width: 150px; max-height: 150px;"
              />
              <button
                type="button"
                class="btn btn-sm btn-outline-danger ms-2"
                onclick={clearProfileImage}>Remove</button
              >
            </div>
          {/if}

          {#if !canSaveProfile}
            <div class="alert alert-info mb-3">
              First name and last name are required.
            </div>
          {/if}

          <button
            type="submit"
            class="btn btn-primary"
            disabled={!canSaveProfile}>Save</button
          >
          <button
            type="button"
            class="btn btn-secondary ms-2"
            onclick={cancelProfileEdit}>Cancel</button
          >
        </form>
      {/if}
    </div>
  </div>

  <!-- Driver Verification Card - nur für Nicht-Admins -->
  {#if !isAdmin}
    <div class="card mb-4">
      <div
        class="card-header d-flex justify-content-between align-items-center"
      >
        <h5 class="mb-0">Driver Verification</h5>
        <div>
          {#if dbUser}
            <span
              class="badge {getStatusClass(dbUser.verificationStatus)} me-2"
            >
              {dbUser.verificationStatus || "UNVERIFIED"}
            </span>
          {/if}
          {#if !isEditingVerification && isProfileComplete}
            <button
              class="btn btn-sm btn-outline-primary"
              onclick={() => (isEditingVerification = true)}
            >
              Edit
            </button>
          {/if}
        </div>
      </div>
      <div class="card-body">
        {#if !isProfileComplete}
          <div class="alert alert-warning mb-0">
            Please complete your profile first before requesting driver
            verification.
          </div>
        {:else if !isEditingVerification}
          <!-- View Mode -->
          {#if dbUser?.verificationStatus === "VERIFIED"}
            <!-- Verified: no alert needed, green badge shows status -->
          {:else if dbUser?.verificationStatus === "PENDING"}
            <div class="alert alert-warning mb-3">
              Your verification is pending. An admin will review your documents
              soon.
            </div>
          {:else if dbUser?.verificationStatus === "DENIED"}
            <div class="alert alert-danger mb-3">
              Your verification was denied. Please upload new documents and try
              again.
            </div>
          {:else}
            <div class="alert alert-info mb-3">
              To offer rides, click "Edit" to upload your driver's license for
              verification.
            </div>
          {/if}

          {#if licenseFrontPreview || licenseBackPreview}
            <div class="row">
              <div class="col-md-6">
                <p class="fw-bold">Front:</p>
                {#if licenseFrontPreview}
                  <img
                    src={licenseFrontPreview}
                    alt="License Front"
                    class="img-fluid rounded border"
                    style="max-height: 200px;"
                  />
                {:else}
                  <span class="text-muted">Not uploaded</span>
                {/if}
              </div>
              <div class="col-md-6">
                <p class="fw-bold">Back:</p>
                {#if licenseBackPreview}
                  <img
                    src={licenseBackPreview}
                    alt="License Back"
                    class="img-fluid rounded border"
                    style="max-height: 200px;"
                  />
                {:else}
                  <span class="text-muted">Not uploaded</span>
                {/if}
              </div>
            </div>
          {/if}
        {:else}
          <!-- Edit Mode -->
          <form method="POST" action="?/requestVerification" use:enhance>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label" for="licenseFront"
                  >Driver's License - Front *</label
                >
                <input
                  class="form-control"
                  id="licenseFront"
                  type="file"
                  accept="image/*"
                  onchange={handleLicenseFrontUpload}
                />
                <input
                  type="hidden"
                  name="licenseImageFront"
                  value={licenseImageFront}
                />
                {#if licenseFrontPreview}
                  <div class="mt-2">
                    <img
                      src={licenseFrontPreview}
                      alt="License Front"
                      class="rounded"
                      style="max-width: 200px;"
                    />
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger ms-2"
                      onclick={clearLicenseFront}>Remove</button
                    >
                  </div>
                {/if}
              </div>
              <div class="col-md-6">
                <label class="form-label" for="licenseBack"
                  >Driver's License - Back *</label
                >
                <input
                  class="form-control"
                  id="licenseBack"
                  type="file"
                  accept="image/*"
                  onchange={handleLicenseBackUpload}
                />
                <input
                  type="hidden"
                  name="licenseImageBack"
                  value={licenseImageBack}
                />
                {#if licenseBackPreview}
                  <div class="mt-2">
                    <img
                      src={licenseBackPreview}
                      alt="License Back"
                      class="rounded"
                      style="max-width: 200px;"
                    />
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger ms-2"
                      onclick={clearLicenseBack}>Remove</button
                    >
                  </div>
                {/if}
              </div>
            </div>

            {#if !canRequestVerification}
              <div class="alert alert-info mb-3">
                Both license images (front and back) are required for
                verification.
              </div>
            {/if}

            {#if dbUser?.verificationStatus === "VERIFIED" || dbUser?.verificationStatus === "PENDING"}
              <p class="text-muted">
                <small
                  >Note: Updating your documents will reset your verification
                  status to PENDING.</small
                >
              </p>
            {/if}

            <button
              type="submit"
              class="btn btn-primary"
              disabled={!canRequestVerification}
            >
              {#if dbUser?.verificationStatus === "VERIFIED" || dbUser?.verificationStatus === "PENDING"}
                Update Verification
              {:else}
                Request Verification
              {/if}
            </button>
            <button
              type="button"
              class="btn btn-secondary ms-2"
              onclick={cancelVerificationEdit}>Cancel</button
            >
          </form>
        {/if}
      </div>
    </div>
  {/if}
{:else}
  <p>Not logged in</p>
{/if}
