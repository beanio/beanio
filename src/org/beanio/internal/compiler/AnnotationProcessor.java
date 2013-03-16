package org.beanio.internal.compiler;

import org.beanio.BeanIOConfigurationException;
import org.beanio.annotation.Record;
import org.beanio.internal.config.*;
import org.beanio.internal.util.TypeUtil;

public class AnnotationProcessor extends ProcessorSupport {

    private ClassLoader classLoader;
    private boolean annotated = false;
    
    
    @Override
    protected void initializeRecord(RecordConfig config) throws BeanIOConfigurationException {
        Class<?> type = TypeUtil.toBeanType(classLoader, config.getType());
        
        Record record = type.getAnnotation(Record.class);
        if (record != null) {
            annotated = true;
            
            if (config.getName() == null) {
                if (!isEmpty(record.name())) {
                    config.setName(record.name());
                }
                else {
                    config.setName(type.getSimpleName());
                }
            }
            
            if (config.getMinOccurs() == null) {
                if (record.minOccurs() >= 0) {
                    config.setMinOccurs(record.minOccurs());
                }
            }
            if (config.getMaxOccurs() == null) {
                if (record.minOccurs() >= 0) {
                    config.setMinOccurs(record.minOccurs());
                }
            }            
            
            
        }
        else {
            annotated = false;
        }
    }

    @Override
    protected void finalizeRecord(RecordConfig record) throws BeanIOConfigurationException {
        // TODO Auto-generated method stub
        super.finalizeRecord(record);
    }

    @Override
    protected void handleField(FieldConfig field) throws BeanIOConfigurationException {
        // TODO Auto-generated method stub
        super.handleField(field);
    }

    private boolean isEmpty(String text) {
        return "".equals(text);
    }
    
}
