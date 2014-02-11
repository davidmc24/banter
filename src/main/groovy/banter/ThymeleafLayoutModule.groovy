package banter

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import nz.net.ultraq.thymeleaf.LayoutDialect
import org.thymeleaf.TemplateEngine
import org.thymeleaf.cache.ICacheManager
import org.thymeleaf.templateresolver.ITemplateResolver

class ThymeleafLayoutModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @SuppressWarnings("UnusedDeclaration")
    @Provides
    @Singleton
    TemplateEngine provideTemplateEngine(ITemplateResolver templateResolver, ICacheManager cacheManager) {
        def templateEngine = new TemplateEngine()
        templateEngine.templateResolver = templateResolver
        templateEngine.cacheManager = cacheManager
        templateEngine.addDialect(new LayoutDialect())
        return templateEngine
    }

}
