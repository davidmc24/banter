package banter

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import nz.net.ultraq.thymeleaf.LayoutDialect
import org.thymeleaf.dialect.IDialect

class ThymeleafLayoutModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), IDialect).addBinding().to(LayoutDialect)
    }

}
