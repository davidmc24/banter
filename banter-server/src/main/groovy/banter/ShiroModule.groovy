package banter

import com.google.inject.Provides
import com.google.inject.Singleton
import org.apache.shiro.realm.text.PropertiesRealm
import ratpack.shiro.ShiroRatpackModule

class ShiroModule extends ShiroRatpackModule {
    @Override
    protected void configureShiroRatpack() {
        bindRealm().to(PropertiesRealm)
    }

    @Provides
    @Singleton
    PropertiesRealm propertiesRealm() {
        def realm = new PropertiesRealm()
        realm.resourcePath = new File("${System.getProperty("user.home")}/.banter/shiro-users.properties").toURI().toString()
        realm.run()
        return realm
    }
}
