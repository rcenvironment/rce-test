/*
 * Copyright (C) 2006-2012 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.validators.internal;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.rcenvironment.core.start.common.validation.PlatformMessage;
import de.rcenvironment.core.start.common.validation.PlatformValidator;
import de.rcenvironment.rce.configuration.ConfigurationService;
/**
 * Validator to ensure the minimum PermGen size.
 * @author Sascha Zur
 * 
 *
 */
public class PermGenMinimumValidator implements PlatformValidator {

    private static ConfigurationService configurationService;

    private static CountDownLatch configurationServiceLatch = new CountDownLatch(1);


    @Deprecated
    public PermGenMinimumValidator() {
        // do nothing
    }

    protected void bindConfigurationService(final ConfigurationService newConfigurationService) {
        PermGenMinimumValidator.configurationService = newConfigurationService;
        configurationServiceLatch.countDown();
    }

    @Override
    public Collection<PlatformMessage> validatePlatform() {
        final Collection<PlatformMessage> result = new LinkedList<PlatformMessage>();

        RuntimeMXBean runtimemxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimemxBean.getInputArguments();
        try {
            String maxPermSize = "";
            for (String str : arguments){
                if (str.startsWith("-XX:MaxPermSize")){
                    maxPermSize = str.substring(str.lastIndexOf('=') + 1);
                }
            }
            int minPermSizeValue = 0;
            if (!maxPermSize.equals("")){
                int maxPermSizeValue = Integer.parseInt(maxPermSize.substring(0, maxPermSize.length() - 1));
                String maxPermSizeUnit = "" + maxPermSize.charAt(maxPermSize.length() - 1);


                PermGenConfiguration config = PermGenMinimumValidator.configurationService
                    .getConfiguration(ValidatorsBundleActivator.bundleSymbolicName, PermGenConfiguration.class);

                minPermSizeValue = 
                    Integer.parseInt(config.getMinimumPermGenSize().substring(0, config.getMinimumPermGenSize().length() - 1));
                String minPermSizeUnit = "" + config.getMinimumPermGenSize().charAt(config.getMinimumPermGenSize().length() - 1);

                if (maxPermSizeUnit.toUpperCase().equals(minPermSizeUnit.toUpperCase())){
                    if (maxPermSizeValue < minPermSizeValue){
                        result.add(new PlatformMessage(PlatformMessage.Type.ERROR,
                            ValidatorsBundleActivator.bundleSymbolicName,
                            Messages.permGenSizeTooLow + config.getMinimumPermGenSize()));
                    }
                }
            }
            long maxPermgen = 0;
            for (MemoryPoolMXBean mx : ManagementFactory.getMemoryPoolMXBeans()) {
                if (mx.getName().endsWith("Perm Gen")) {
                    maxPermgen = mx.getUsage().getMax()/(long) Math.pow(2, 2 * 10);
                }
            }
            if (maxPermgen != 0 && maxPermgen < minPermSizeValue) {
                result.add(new PlatformMessage(PlatformMessage.Type.ERROR,
                    ValidatorsBundleActivator.bundleSymbolicName,
                    Messages.permGenSizeTooLow + minPermSizeValue));
            }

        } catch (NullPointerException e){
            return result;
        }
        return result;
    }
    /*
     * Please note: There is not unit test for this method, because too much OSGi
     * API mocking would be needed for that little bit of code. It is tested
     * well, because it runs every time RCE starts up.
     */
    protected ConfigurationService getConfigurationService() {
        try {
            PermGenMinimumValidator.configurationServiceLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return PermGenMinimumValidator.configurationService;
    }

}
