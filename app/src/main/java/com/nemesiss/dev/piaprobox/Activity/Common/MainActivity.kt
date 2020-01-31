package com.nemesiss.dev.piaprobox.Activity.Common

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.nemesiss.dev.piaprobox.Fragment.Main.*
import com.nemesiss.dev.piaprobox.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : PiaproboxBaseActivity() {

    private val MainFragmentsCollection: HashMap<Int, BaseMainFragment> = HashMap()

    private lateinit var CurrentShowMainFragment: BaseMainFragment

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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.Main_Toolbar_Reload -> {
                CurrentShowMainFragment.Refresh()
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
                RecommendFragment::class.java
            )
        )
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
                    // 保证单例
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


    override fun onDestroy() {
        MainFragmentsCollection.clear()
        super.onDestroy()
    }
}
