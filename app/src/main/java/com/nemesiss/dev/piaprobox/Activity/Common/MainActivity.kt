package com.nemesiss.dev.piaprobox.Activity.Common

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Priority
import com.nemesiss.dev.piaprobox.Activity.Music.MusicControlActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity
import com.nemesiss.dev.piaprobox.Activity.TestSkeletonActivity
import com.nemesiss.dev.piaprobox.Fragment.BaseMainFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Illustrator.IllustrationFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Music.MusicFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories.RecommendImageCategoryFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.MainRecommendFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.TextWork.TextWorkFragment
import com.nemesiss.dev.piaprobox.Model.Events.MusicPlayerClosedEvent
import com.nemesiss.dev.piaprobox.Model.Resources.Constants
import com.nemesiss.dev.piaprobox.Model.User.LoginResult
import com.nemesiss.dev.piaprobox.Model.User.LoginStatus
import com.nemesiss.dev.piaprobox.Model.User.UserInfo
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerUserLoginServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.CookieLoginService
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HtmlParserModules
import com.nemesiss.dev.piaprobox.Service.GlideApp
import com.nemesiss.dev.piaprobox.Service.Player.MusicPlayerService
import com.nemesiss.dev.piaprobox.Service.User.NotLoginException
import com.nemesiss.dev.piaprobox.Service.User.UserLoginService
import com.nemesiss.dev.piaprobox.Util.AppUtil
import com.nemesiss.dev.piaprobox.View.Common.UserInfoActionsSheet
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.getLogger
import javax.inject.Inject

class MainActivity : LoginCallbackActivity() {

    companion object {
        @JvmStatic
        val MainFragmentIDs = arrayOf(
            R.id.Main_Drawer_Nav_Music,
            R.id.Main_Drawer_Nav_Illustrator,
            R.id.Main_Drawer_Nav_Text,
            R.id.Main_Drawer_Nav_Recommand
        ).zip(
            arrayOf(
                MusicFragment::class.java,
                IllustrationFragment::class.java,
                TextWorkFragment::class.java,
                MainRecommendFragment::class.java
            )
        )

        @JvmStatic
        private val MUSIC_PLAYER_MENU_ID = Int.MAX_VALUE - 100
    }

    @Inject
    @CookieLoginService
    lateinit var userLoginService: UserLoginService

    private val MainFragmentsCollection: HashMap<Int, BaseMainFragment> = HashMap()
    private lateinit var CurrentShowMainFragment: BaseMainFragment

    private val asyncExecutor = AsyncExecutor.INSTANCE
    private val log = getLogger<MainActivity>()

    var DisableRefreshButton = false
    private val SHOW_MAIN_PAGES = true

    private var navHeaderUserAvatarIv: CircleImageView? = null
    private var navHeaderNickNameTv: TextView? = null
    private lateinit var musicPlayerStatusDisposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DaggerUserLoginServiceFactory.builder().htmlParserModules(HtmlParserModules(this)).build().inject(this)
        InitView()
        musicPlayerStatusDisposable =
            MusicPlayerService.SERVICE_AVAILABLE.subscribe(this::handleMusicPlayerClosedByNotificationBtn)
    }

    override fun onDestroy() {
        MainFragmentsCollection.clear()
        if (!musicPlayerStatusDisposable.isDisposed) {
            musicPlayerStatusDisposable.dispose()
        }
        super.onDestroy()
    }

    override fun handleLoginResult(loginResult: LoginResult, userInfo: UserInfo?) {
        if (loginResult == LoginResult.SUCCESS) {
            log.info("请求登陆成功, 登陆用户为: {}", userInfo)
            setUserInfoIfLogin()
            return
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_right_menu, menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        invalidateOptionsMenu()
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
    }

    override fun onBackPressed() {
        moveTaskToBack(false)
    }

    // 管理Toolbar上面的播放按钮是不是存在.
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (MusicPlayerService.SERVICE_AVAILABLE.value == true && menu?.findItem(MUSIC_PLAYER_MENU_ID) == null) {
            val playerMenu = menu?.add(Menu.NONE, MUSIC_PLAYER_MENU_ID, 2, "Music Player")
            playerMenu?.setIcon(R.drawable.ic_play_circle_outline_white_24dp)
            playerMenu?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        } else if (MusicPlayerService.SERVICE_AVAILABLE.value != true && menu?.findItem(MUSIC_PLAYER_MENU_ID) != null) {
            menu.removeItem(MUSIC_PLAYER_MENU_ID)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.Main_Toolbar_Reload -> {
                if (!DisableRefreshButton)
                    CurrentShowMainFragment.Refresh()
            }
            MUSIC_PLAYER_MENU_ID -> {
                val intent = Intent(this, MusicControlActivity::class.java)
                intent.putExtra(MusicPlayerActivity.CLICK_TOOLBAR_ICON, true)
                startActivity(intent)
            }
        }
        return true
    }

    private fun InitView() {
        setSupportActionBar(Main_Toolbar)
        Main_Toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        Main_Toolbar.setNavigationOnClickListener { Main_Drawer.openDrawer(GravityCompat.START) }
        Main_Drawer_Navigation.setNavigationItemSelectedListener(this::OnNavigationItemSelected)
        if (SHOW_MAIN_PAGES) {
            LoadMainFragmentFromCache(R.id.Main_Drawer_Nav_Recommand)
        }
        bindNavHeaderUserInfoAreaActions()
        setUserInfoIfLogin()
    }

    private fun bindNavHeaderUserInfoAreaActions() {
        val hc = Main_Drawer_Navigation.headerCount
        if (hc > 0) {
            val navHeaderUserInfoAreaLayout = Main_Drawer_Navigation.getHeaderView(0)
                .findViewById<LinearLayout>(R.id.nav_header_click_login)
            navHeaderUserInfoAreaLayout.apply {
                setOnClickListener(this@MainActivity::onNavHeaderUserInfoAreaClicked)
                navHeaderUserAvatarIv = findViewById(R.id.nav_header_user_avatar)
                navHeaderNickNameTv = findViewById(R.id.nav_header_username)
            }
        }
    }

    private fun onNavHeaderUserInfoAreaClicked(v: View) {
        if (userLoginService.checkLogin() != LoginStatus.LOGIN) {
            userLoginService.startLoginActivity(this)
            return
        }

        UserInfoActionsSheet().apply {
            setOnItemClickedListener {
                when (it.itemId) {
                    R.id.open_userinfo_in_browser -> {
                        AppUtil.OpenBrowser(
                            this@MainActivity,
                            Constants.Url.getUserProfileUrl(userLoginService.getUserInfo().userName)
                        )
                    }
                    R.id.user_logout -> {
                        asyncExecutor.SendTask {
                            runOnUiThread { resetUserInfoToPendingLogin() }
                            userLoginService.logout()
                        }
                    }
                }
                dismiss()
                true
            }
            show(supportFragmentManager, "USERINFO_ACTIONS")
        }
    }

    private fun resetUserInfoToPendingLogin() {
        navHeaderUserAvatarIv?.setImageDrawable(getDrawable(R.drawable.not_login))
        navHeaderNickNameTv?.text = getString(R.string.not_login)
    }

    private fun setUserInfoIfLogin() {
        if (userLoginService.checkLogin(useCache = true) == LoginStatus.LOGIN) {
            asyncExecutor.SendTask {
                try {
                    val userInfo = userLoginService.getUserInfo()
                    runOnUiThread { setUserInfo(userInfo) }
                } catch (nle: NotLoginException) {
                    // 没有登陆, 处理登录态失效, 直接做登出
                    userLoginService.logout()
                    log.warn("Login status is expired, now doing manually logout to correct this.")
                } catch (e: Exception) {
                    log.error("Get user info failed!", e)
                    runOnUiThread {
                        Toast.makeText(this, "获取用户信息失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setUserInfo(userInfo: UserInfo) {
        // load user avatar
        navHeaderUserAvatarIv?.let { iv ->
            GlideApp
                .with(this)
                .load(userInfo.avatarImage)
                .priority(Priority.HIGH)
                .into(iv)
        }
        // load user nickname instead of username
        // because username is usually meaningless.
        navHeaderNickNameTv?.text = userInfo.nickName
    }


    private fun OnNavigationItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.debug_tools) {
            startActivity(Intent(this, TestSkeletonActivity::class.java))
            return true
        }
        LoadMainFragmentFromCache(item?.itemId)
        Main_Drawer.closeDrawers()
        return true
    }

    @Synchronized
    private fun LoadMainFragmentFromCache(FragmentID: Int?) {
        MainFragmentIDs
            .single { it.first == FragmentID }
            .run {
                var fragment = MainFragmentsCollection[this.first]
                if (fragment == null) {
                    //  保证单例
                    fragment = this.second.newInstance()
                    MainFragmentsCollection[this.first] = fragment
                }
                ShowMainFragment(fragment!!)
            }
    }

    private fun ShowMainFragment(fragment: BaseMainFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.Main_Fragment_Container, fragment)
        transaction.commit()
        CurrentShowMainFragment = fragment
    }

    // 向对应Fragment转发看图Activity退出时的Intent，辅助完成共享元素回缩的效果.
    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        when (CurrentShowMainFragment) {
            is MainRecommendFragment -> {
                val mainFragment = CurrentShowMainFragment as MainRecommendFragment
                if (mainFragment.CurrentDisplayFragment() is RecommendImageCategoryFragment) {
                    val recommendImageCategoryFragment =
                        mainFragment.CurrentDisplayFragment() as RecommendImageCategoryFragment
                    recommendImageCategoryFragment.onActivityReenter(resultCode, data)
                }
            }
            is IllustrationFragment -> {
                val illuFragment = CurrentShowMainFragment as IllustrationFragment
                illuFragment.onActivityReenter(resultCode, data)
            }
        }
    }

    private fun handleMusicPlayerClosedByNotificationBtn(available: Boolean) {
        invalidateOptionsMenu()
    }
}
