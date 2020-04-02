/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.model.Product;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
@NbBundle.Messages({"ImportTargetFactory.createInstance.exception.message=An exception has occurred creating an ImportTarget.Factory for {0} and product {1}."})
public abstract class ImportTargetFactory<I extends ImportTarget> {

    protected final Product product;
    protected final Class<I> type;

    protected ImportTargetFactory(final Product product, final Class<I> subType) {
        this.product = product;
        this.type = subType;
    }

    public Product getProduct() {
        return product;
    }

    public I createInstance(final String provider, final Class<I> subType) throws IOException {
        if (type.isAssignableFrom(subType)) {
            doCreateInstance(provider);
        }
        throw new NoProductProviderException(provider, getProduct());
    }

    protected abstract I doCreateInstance(String provider) throws IOException;

    public abstract List<ImportTargetFactory<I>.ProviderRef> available(final Class<I> subType);

    public class ProviderRef {

        protected final String provider;

        public ProviderRef(String provider) {
            this.provider = provider;
        }

        public String getProvider() {
            return provider;
        }

        public ImportTargetFactory<I> getFactory() {
            return ImportTargetFactory.this;
        }

        public I createInstance() throws IOException {
            return doCreateInstance(provider);
        }
    }

    @NbBundle.Messages({"NoProductProviderException.message=No ImportTarget.Factory for provider \"{0}\" and product \"{1}\" could be found."})
    static class NoProductProviderException extends NoProviderException {

        private final Product product;

        NoProductProviderException(String url, Product prod) {
            super(ImportTargetFactory.class, url);
            this.product = prod;
        }

        @Override
        public String getMessage() {
            return NbBundle.getMessage(NoProductProviderException.class, "NoProductProviderException.message", url, product.getDisplay());
        }
    }

    public static <F extends ImportTarget> F find(final String url, final Class<F> subType, final Product prod) {
        return Lookup.getDefault().lookupAll(ImportTargetFactory.class).stream()
                .filter(sbit -> prod == null || sbit.getProduct().equals(prod))
                .map(sbit -> sbit)
                .flatMap(sbit -> (Stream<ImportTargetFactory<F>.ProviderRef>) sbit.available(subType).stream())
                .filter(p -> p.getProvider().equals(url))
                .collect(CollectionUtil.requireSingleton())
                .map(sbit -> {
                    try {
                        return sbit.createInstance();
                    } catch (IOException e) {
                        final String msg = NbBundle.getMessage(ImportTargetFactory.class, "ImportTargetFactory.createInstance.exception.message", url, prod.getDisplay());
                        PlatformUtil.getCodeNameBaseLogger(ImportTargetFactory.class).log(LogLevel.INFO_WARNING, msg);
                        return null;
                    }
                })
                .orElseThrow(() -> new NoProductProviderException(url, prod));
    }

}
