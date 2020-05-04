package com.nemesiss.dev.piaprobox.Activity.Common

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.Menu
import android.view.MenuItem
import com.nemesiss.dev.piaprobox.Activity.Music.MusicControlActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Illustrator.IllustratorFragment
import com.nemesiss.dev.piaprobox.Fragment.Main.*
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Music.MusicFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories.RecommendImageCategoryFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.MainRecommendFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.TextWork.TextWorkFragment
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerService
import com.nemesiss.dev.piaprobox.Util.AppUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : PiaproboxBaseActivity() {

    private val MainFragmentsCollection: HashMap<Int, BaseMainFragment> = HashMap()
    private lateinit var CurrentShowMainFragment: BaseMainFragment


    override fun onDestroy() {
        MainFragmentsCollection.clear()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        InitView()
    }

    private fun InitView() {
        setSupportActionBar(Main_Toolbar)
        Main_Toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        Main_Toolbar.setNavigationOnClickListener { Main_Drawer.openDrawer(GravityCompat.START) }
        Main_Drawer_Navigation.setNavigationItemSelectedListener(this::OnNavigationItemSelected)
        LoadMainFragmentFromCache(R.id.Main_Drawer_Nav_Recommand)
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if ((MusicPlayerService.SERVICE_AVAILABLE.value == true ||
                    AppUtil.IsServiceRunning(this, MusicPlayerService::class.java))
            && menu?.findItem(MUSIC_PLAYER_MENU_ID) == null
        ) {
            val playerMenu = menu?.add(Menu.NONE, MUSIC_PLAYER_MENU_ID, 2, "Music Player")
            playerMenu?.setIcon(R.drawable.ic_play_circle_outline_white_24dp)
            playerMenu?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
//            Log.d("MainActivity", "onPrepareOptionsMenu 创建播放按钮")
        } else if (MusicPlayerService.SERVICE_AVAILABLE.value != true && menu?.findItem(MUSIC_PLAYER_MENU_ID) != null) {
            menu.removeItem(MUSIC_PLAYER_MENU_ID)
//            Log.d("MainActivity", "onPrepareOptionsMenu 取消播放按钮")
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.Main_Toolbar_Reload -> {
                CurrentShowMainFragment.Refresh()
            }
            MUSIC_PLAYER_MENU_ID -> {
                val intent = Intent(PiaproboxApplication.Self.applicationContext, MusicControlActivity::class.java)
                intent.putExtra(MusicPlayerActivity.CLICK_TOOLBAR_ICON, true)
                startActivity(intent)
            }
        }
        return true
    }

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
                IllustratorFragment::class.java,
                TextWorkFragment::class.java,
                MainRecommendFragment::class.java
            )
        )

        @JvmStatic
        private val MUSIC_PLAYER_MENU_ID = Int.MAX_VALUE - 100
    }

    private fun OnNavigationItemSelected(item: MenuItem?): Boolean {
        LoadMainFragmentFromCache(item?.itemId)
        Main_Drawer.closeDrawers()
        return true
    }

    private fun LoadMainFragmentFromCache(FragmentID: Int?) {
        MainFragmentIDs
            .single { it.first == FragmentID }
            .run {
                var fragment = MainFragmentsCollection[this.first]
                if (fragment == null) {
//                    保证单例
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

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        ((CurrentShowMainFragment as? MainRecommendFragment)
            ?.CurrentDisplayFragment() as? RecommendImageCategoryFragment)
            ?.onActivityReenter(resultCode, data)
    }
}
