/* Copyright 2018 Conny Duck
 *
 * This file is a part of Tusky.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky; if not,
 * see <http://www.gnu.org/licenses>. */

package com.keylesspalace.tusky.components.preference

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.keylesspalace.tusky.R
import com.keylesspalace.tusky.db.AccountManager
import com.keylesspalace.tusky.di.Injectable
import com.keylesspalace.tusky.entity.Notification
import com.keylesspalace.tusky.settings.AppTheme
import com.keylesspalace.tusky.settings.PrefKeys
import com.keylesspalace.tusky.settings.emojiPreference
import com.keylesspalace.tusky.settings.listPreference
import com.keylesspalace.tusky.settings.makePreferenceScreen
import com.keylesspalace.tusky.settings.preference
import com.keylesspalace.tusky.settings.preferenceCategory
import com.keylesspalace.tusky.settings.switchPreference
import com.keylesspalace.tusky.util.LocaleManager
import com.keylesspalace.tusky.util.deserialize
import com.keylesspalace.tusky.util.makeIcon
import com.keylesspalace.tusky.util.serialize
import com.keylesspalace.tusky.util.unsafeLazy
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import de.c1710.filemojicompat_ui.views.picker.preference.EmojiPickerPreference
import javax.inject.Inject

class PreferencesFragment : PreferenceFragmentCompat(), Injectable {

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var localeManager: LocaleManager

    private val iconSize by unsafeLazy { resources.getDimensionPixelSize(R.dimen.preference_icon_size) }

    enum class ReadingOrder {
        /** User scrolls up, reading statuses oldest to newest */
        OLDEST_FIRST,

        /** User scrolls down, reading statuses newest to oldest. Default behaviour. */
        NEWEST_FIRST;

        companion object {
            fun from(s: String?): ReadingOrder {
                s ?: return NEWEST_FIRST

                return try {
                    valueOf(s.uppercase())
                } catch (_: Throwable) {
                    NEWEST_FIRST
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        makePreferenceScreen {
            preferenceCategory(R.string.pref_title_appearance_settings) {
                listPreference {
                    setDefaultValue(AppTheme.NIGHT.value)
                    setEntries(R.array.app_theme_names)
                    entryValues = AppTheme.stringValues()
                    key = PrefKeys.APP_THEME
                    setSummaryProvider { entry }
                    setTitle(R.string.pref_title_app_theme)
                    icon = makeIcon(GoogleMaterial.Icon.gmd_palette)
                }

                emojiPreference(requireActivity()) {
                    setTitle(R.string.emoji_style)
                    icon = makeIcon(GoogleMaterial.Icon.gmd_sentiment_satisfied)
                }

                listPreference {
                    setDefaultValue("default")
                    setEntries(R.array.language_entries)
                    setEntryValues(R.array.language_values)
                    key = PrefKeys.LANGUAGE + "_" // deliberately not the actual key, the real handling happens in LocaleManager
                    setSummaryProvider { entry }
                    setTitle(R.string.pref_title_language)
                    icon = makeIcon(GoogleMaterial.Icon.gmd_translate)
                    preferenceDataStore = localeManager
                }

                listPreference {
                    setDefaultValue("medium")
                    setEntries(R.array.post_text_size_names)
                    setEntryValues(R.array.post_text_size_values)
                    key = PrefKeys.STATUS_TEXT_SIZE
                    setSummaryProvider { entry }
                    setTitle(R.string.pref_post_text_size)
                    icon = makeIcon(GoogleMaterial.Icon.gmd_format_size)
                }

                listPreference {
                    setDefaultValue(ReadingOrder.NEWEST_FIRST.name)
                    setEntries(R.array.reading_order_names)
                    setEntryValues(R.array.reading_order_values)
                    key = PrefKeys.READING_ORDER
                    setSummaryProvider { entry }
                    setTitle(R.string.pref_title_reading_order)
                    icon = makeIcon(GoogleMaterial.Icon.gmd_sort)
                }

                listPreference {
                    setDefaultValue("top")
                    setEntries(R.array.pref_main_nav_position_options)
                    setEntryValues(R.array.pref_main_nav_position_values)
                    key = PrefKeys.MAIN_NAV_POSITION
                    setSummaryProvider { entry }
                    setTitle(R.string.pref_main_nav_position)
                }

                listPreference {
                    setDefaultValue("disambiguate")
                    setEntries(R.array.pref_show_self_username_names)
                    setEntryValues(R.array.pref_show_self_username_values)
                    key = PrefKeys.SHOW_SELF_USERNAME
                    setSummaryProvider { entry }
                    setTitle(R.string.pref_title_show_self_username)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(false)
                    key = PrefKeys.HIDE_TOP_TOOLBAR
                    setTitle(R.string.pref_title_hide_top_toolbar)
                }

                switchPreference {
                    setDefaultValue(false)
                    key = PrefKeys.FAB_HIDE
                    setTitle(R.string.pref_title_hide_follow_button)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(false)
                    key = PrefKeys.ABSOLUTE_TIME_VIEW
                    setTitle(R.string.pref_title_absolute_time)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(true)
                    key = PrefKeys.SHOW_BOT_OVERLAY
                    setTitle(R.string.pref_title_bot_overlay)
                    isSingleLineTitle = false
                    setIcon(R.drawable.ic_bot_24dp)
                }

                switchPreference {
                    setDefaultValue(false)
                    key = PrefKeys.ANIMATE_GIF_AVATARS
                    setTitle(R.string.pref_title_animate_gif_avatars)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(false)
                    key = PrefKeys.ANIMATE_CUSTOM_EMOJIS
                    setTitle(R.string.pref_title_animate_custom_emojis)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(true)
                    key = PrefKeys.USE_BLURHASH
                    setTitle(R.string.pref_title_gradient_for_media)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(false)
                    key = PrefKeys.SHOW_CARDS_IN_TIMELINES
                    setTitle(R.string.pref_title_show_cards_in_timelines)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(true)
                    key = PrefKeys.SHOW_NOTIFICATIONS_FILTER
                    setTitle(R.string.pref_title_show_notifications_filter)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(true)
                    key = PrefKeys.CONFIRM_REBLOGS
                    setTitle(R.string.pref_title_confirm_reblogs)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(false)
                    key = PrefKeys.CONFIRM_FAVOURITES
                    setTitle(R.string.pref_title_confirm_favourites)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(true)
                    key = PrefKeys.ENABLE_SWIPE_FOR_TABS
                    setTitle(R.string.pref_title_enable_swipe_for_tabs)
                    isSingleLineTitle = false
                }

                switchPreference {
                    setDefaultValue(false)
                    key = PrefKeys.SHOW_STATS_INLINE
                    setTitle(R.string.pref_title_show_stat_inline)
                    isSingleLineTitle = false
                }
            }

            preferenceCategory(R.string.pref_title_browser_settings) {
                switchPreference {
                    setDefaultValue(false)
                    key = PrefKeys.CUSTOM_TABS
                    setTitle(R.string.pref_title_custom_tabs)
                    isSingleLineTitle = false
                }
            }

            preferenceCategory(R.string.pref_title_timeline_filters) {
                preference {
                    setTitle(R.string.pref_title_post_tabs)
                    fragment = TabFilterPreferencesFragment::class.qualifiedName
                }
            }

            preferenceCategory(R.string.pref_title_wellbeing_mode) {
                switchPreference {
                    title = getString(R.string.limit_notifications)
                    setDefaultValue(false)
                    key = PrefKeys.WELLBEING_LIMITED_NOTIFICATIONS
                    setOnPreferenceChangeListener { _, value ->
                        for (account in accountManager.accounts) {
                            val notificationFilter = deserialize(account.notificationsFilter).toMutableSet()

                            if (value == true) {
                                notificationFilter.add(Notification.Type.FAVOURITE)
                                notificationFilter.add(Notification.Type.FOLLOW)
                                notificationFilter.add(Notification.Type.REBLOG)
                            } else {
                                notificationFilter.remove(Notification.Type.FAVOURITE)
                                notificationFilter.remove(Notification.Type.FOLLOW)
                                notificationFilter.remove(Notification.Type.REBLOG)
                            }

                            account.notificationsFilter = serialize(notificationFilter)
                            accountManager.saveAccount(account)
                        }
                        true
                    }
                }

                switchPreference {
                    title = getString(R.string.wellbeing_hide_stats_posts)
                    setDefaultValue(false)
                    key = PrefKeys.WELLBEING_HIDE_STATS_POSTS
                }

                switchPreference {
                    title = getString(R.string.wellbeing_hide_stats_profile)
                    setDefaultValue(false)
                    key = PrefKeys.WELLBEING_HIDE_STATS_PROFILE
                }
            }

            preferenceCategory(R.string.pref_title_proxy_settings) {
                preference {
                    setTitle(R.string.pref_title_http_proxy_settings)
                    fragment = ProxyPreferencesFragment::class.qualifiedName
                    summaryProvider = ProxyPreferencesFragment.SummaryProvider
                }
            }
        }
    }

    private fun makeIcon(icon: GoogleMaterial.Icon): IconicsDrawable {
        return makeIcon(requireContext(), icon, iconSize)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.action_view_preferences)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (!EmojiPickerPreference.onDisplayPreferenceDialog(this, preference)) {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    companion object {
        fun newInstance(): PreferencesFragment {
            return PreferencesFragment()
        }
    }
}
