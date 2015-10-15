/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample.android.testdpc.profilepolicy.apprestrictions;

import android.content.RestrictionEntry;
import android.content.RestrictionsManager;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sample.android.testdpc.DeviceAdminReceiver;
import com.sample.android.testdpc.R;

import java.util.List;

/**
 * This fragment shows all installed apps and allows viewing and editing application restrictions
 * for those apps. It also allows loading the default app restrictions for each of those apps.
 */
public class ManageAppRestrictionsFragment extends BaseAppRestrictionsFragment {

    public static ManageAppRestrictionsFragment newInstance() {
        ManageAppRestrictionsFragment fragment = new ManageAppRestrictionsFragment();
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(layoutInflater, container, savedInstanceState);
        View loadDefaultButton = view.findViewById(R.id.load_default_button);
        loadDefaultButton.setVisibility(View.VISIBLE);
        loadDefaultButton.setOnClickListener(this);
        updateViewVisibilities(view);
        return view;
    }

    @Override
    protected void loadData(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            Bundle bundle = mDevicePolicyManager.getApplicationRestrictions(
                    DeviceAdminReceiver.getComponentName(getActivity()), pkgName);

            loadAppRestrictionsList(convertBundleToRestrictions(bundle));
        }
    }

    protected String getCurrentAppName() {
        ApplicationInfo applicationInfo =
                (ApplicationInfo) mManagedAppsSpinner.getSelectedItem();
        return (String) getActivity().getPackageManager().getApplicationLabel(
                applicationInfo);
    }

    private void loadManifestAppRestrictions(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            List<RestrictionEntry> manifestRestrictions = null;
            try {
                manifestRestrictions = mRestrictionsManager.getManifestRestrictions(pkgName);
                convertTypeChoiceAndNullToString(manifestRestrictions);
            } catch (NullPointerException e) {
                // This means no default restrictions.
            }
            if (manifestRestrictions != null) {
                loadAppRestrictionsList(manifestRestrictions.toArray(new RestrictionEntry[0]));
            }
        }
    }

    /**
     * TODO (b/23378519): Remove this method and add support for type choice and null.
     */
    private void convertTypeChoiceAndNullToString(List<RestrictionEntry> restrictionEntries) {
        for (RestrictionEntry entry : restrictionEntries) {
            if (entry.getType() == RestrictionEntry.TYPE_CHOICE ||
                    entry.getType() == RestrictionEntry.TYPE_NULL) {
                entry.setType(RestrictionEntry.TYPE_STRING);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_app:
                String pkgName =
                        ((ApplicationInfo) mManagedAppsSpinner.getSelectedItem()).packageName;
                mDevicePolicyManager.setApplicationRestrictions(
                        DeviceAdminReceiver.getComponentName(getActivity()), pkgName,
                        convertRestrictionsToBundle(mRestrictionEntries));
                showToast(getString(R.string.set_app_restrictions_success, pkgName));
                break;
            case R.id.load_default_button:
                loadManifestAppRestrictions(
                        ((ApplicationInfo) mManagedAppsSpinner.getSelectedItem()).packageName);
                break;
            default:
                super.onClick(v);
        }
    }

    /**
     * Wrapper for RestrictionsManager.convertRestrictionsToBundle, with a fallback implementation
     * for versions before M where this was introduced.
     * The fallback recognises only the restriction types available on L.
     */
    private Bundle convertRestrictionsToBundle(List<RestrictionEntry> restrictionEntries) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return RestrictionsManager.convertRestrictionsToBundle(restrictionEntries);
        }
        Bundle bundle = new Bundle();
        for (RestrictionEntry entry : restrictionEntries) {
            switch (entry.getType()) {
                case RestrictionEntry.TYPE_BOOLEAN:
                    bundle.putBoolean(entry.getKey(), entry.getSelectedState());
                    break;
                case RestrictionEntry.TYPE_INTEGER:
                    bundle.putInt(entry.getKey(), entry.getIntValue());
                    break;
                case RestrictionEntry.TYPE_STRING:
                case RestrictionEntry.TYPE_NULL:
                    bundle.putString(entry.getKey(), entry.getSelectedString());
                    break;
                case RestrictionEntry.TYPE_CHOICE:
                case RestrictionEntry.TYPE_MULTI_SELECT:
                    bundle.putStringArray(entry.getKey(), entry.getAllSelectedStrings());
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unsupported restrictionEntry type: " + entry.getType());
            }
        }
        return bundle;
    }
}