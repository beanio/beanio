package org.beanio.internal.compiler.accessor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;

import org.beanio.BeanIOException;
import org.beanio.internal.compiler.PropertyAccessorFactory;
import org.beanio.internal.parser.PropertyAccessor;
import org.beanio.internal.parser.accessor.*;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;

/**
 * {@link PropertyAccessorFactory} that uses ASM to generate {@link PropertyAccessor}
 * implementations.
 * 
 * @author Kevin Seim
 * @since 2.0.1
 */
public class AsmAccessorFactory implements Opcodes, PropertyAccessorFactory {

    private static final String CG_PACKAGE = "org/beanio/internal/cg/";
    
    private static final Type supportType = Type.getType(PropertyAccessorSupport.class);
    
    private AsmClassLoader classLoader;

    /**
     * Constructs a new <tt>AsmAccessorFactory</tt>.
     * @param classLoader the {@link ClassLoader} to use
     */
    public AsmAccessorFactory(ClassLoader classLoader) {
        this.classLoader = new AsmClassLoader(getClass().getClassLoader());
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.compiler.PropertyAccessorFactory#getPropertyAccessor(java.lang.Class, java.beans.PropertyDescriptor, int)
     */
    public PropertyAccessor getPropertyAccessor(
        Class<?> parent, PropertyDescriptor descriptor, int carg) {
        
        return getPropertyAccessor(parent, 
            descriptor.getPropertyType(), 
            descriptor.getName(),
            descriptor.getReadMethod(),
            descriptor.getWriteMethod(),
            null, 
            carg);
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.compiler.PropertyAccessorFactory#getPropertyAccessor(java.lang.Class, java.lang.reflect.Field, int)
     */
    public PropertyAccessor getPropertyAccessor(
        Class<?> parent, Field field, int carg) {
    
        return getPropertyAccessor(parent, 
            field.getType(), 
            field.getName(),
            null,
            null,
            field, 
            carg);
    }
    
    private PropertyAccessor getPropertyAccessor(
        Class<?> parent, 
        Class<?> propertyClass,
        String propertyName,
        Method readMethod, 
        Method writeMethod,
        Field field,
        int carg) {
            
        Type parentType = Type.getType(parent);
        Type accessorType = Type.getObjectType(
            CG_PACKAGE + parentType.getInternalName() + "_" + propertyName + "_PropertyAccessorImpl");
        
        PropertyAccessorSupport support = createAccessor(accessorType.getClassName());
        if (support != null) {
            support.setConstructorArgumentIndex(carg);
            return support;
        }
        
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, accessorType.getInternalName(), 
            null, "org/beanio/internal/parser/accessor/PropertyAccessorSupport", null);
        
        // create constructor
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, supportType.getInternalName(), "<init>", "()V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        
        // create getValue()
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getValue", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            mv.visitCode();
            if (readMethod != null) {
                invokeGetter(mv, readMethod.getName(), parentType, parent, propertyClass);
            }
            else if (field != null) {
                invokeGetField(mv, propertyName, parentType, propertyClass);
            }
            else {
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 2);
            }
            mv.visitEnd();
        }
        
        // create setValue()
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            if (writeMethod != null) {
                invokeSetter(mv, writeMethod.getName(), parentType, propertyClass);
            }
            else if (field != null) {
                invokeSetField(mv, propertyName, parentType, propertyClass);
            }
            else {
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 3);                
            }
            mv.visitEnd();
        }
        cw.visitEnd();

        support = createAccessor(classLoader.defineClass(
            accessorType.getClassName(), cw.toByteArray()));
        support.setConstructorArgumentIndex(carg);
        return support;
    }

    
    private void invokeGetter(MethodVisitor mv, String method, 
        Type parent, Class<?> parentClass, Class<?> property) {
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, parent.getInternalName());
        
        int opcode = parentClass.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL;
        
        if (!property.isPrimitive()) {
            mv.visitMethodInsn(opcode, parent.getInternalName(), 
                method, "()" + Type.getType(property).getReturnType());
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == int.class) {
            mv.visitMethodInsn(opcode, parent.getInternalName(), method, "()I");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == long.class) {
            mv.visitMethodInsn(opcode, parent.getInternalName(), method, "()J");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");    
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 2);
        }
        else if (property == boolean.class) {
            mv.visitMethodInsn(opcode, parent.getInternalName(), method, "()Z");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == char.class) {
            mv.visitMethodInsn(opcode, parent.getInternalName(), method, "()C");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");    
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == double.class) {
            mv.visitMethodInsn(opcode, parent.getInternalName(), method, "()D");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 2);
        }
        else if (property == float.class) {
            mv.visitMethodInsn(opcode, parent.getInternalName(), method, "()F");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");   
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == short.class) {
            mv.visitMethodInsn(opcode, parent.getInternalName(), method, "()S");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");   
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == byte.class) {
            mv.visitMethodInsn(opcode, parent.getInternalName(), method, "()B");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");  
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
    }
    
    private void invokeGetField(MethodVisitor mv, String field, Type parent, Class<?> property) {
        
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, parent.getInternalName());
        
        if (!property.isPrimitive()) {
            Type type = Type.getType(property);
            mv.visitFieldInsn(GETFIELD, parent.getInternalName(), field, type.getReturnType().toString());
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == int.class) {
            mv.visitFieldInsn(GETFIELD, parent.getInternalName(), field, "I");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == long.class) {
            mv.visitFieldInsn(GETFIELD, parent.getInternalName(), field, "J");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");    
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 2);
        }
        else if (property == boolean.class) {
            mv.visitFieldInsn(GETFIELD, parent.getInternalName(), field, "Z");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == char.class) {
            mv.visitFieldInsn(GETFIELD, parent.getInternalName(), field, "C");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");    
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == double.class) {
            mv.visitFieldInsn(GETFIELD, parent.getInternalName(), field, "D");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 2);
        }
        else if (property == float.class) {
            mv.visitFieldInsn(GETFIELD, parent.getInternalName(), field, "F");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");   
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == short.class) {
            mv.visitFieldInsn(GETFIELD, parent.getInternalName(), field, "S");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");   
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
        else if (property == byte.class) {
            mv.visitFieldInsn(GETFIELD, parent.getInternalName(), field, "B");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");  
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
        }
    }
    
    
    private void invokeSetter(MethodVisitor mv, String method, Type parent, Class<?> property) {
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, parent.getInternalName());
        mv.visitVarInsn(ALOAD, 2);
        
        if (!property.isPrimitive()) {
            Type type = Type.getType(property);
            
            mv.visitTypeInsn(CHECKCAST, type.getInternalName());
            mv.visitMethodInsn(INVOKEVIRTUAL, parent.getInternalName(), 
                method, "(" + type.getReturnType() + ")V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == boolean.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
            mv.visitMethodInsn(INVOKEVIRTUAL, parent.getInternalName(), method, "(Z)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == int.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
            mv.visitMethodInsn(INVOKEVIRTUAL, parent.getInternalName(), method, "(I)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == long.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
            mv.visitMethodInsn(INVOKEVIRTUAL, parent.getInternalName(), method, "(J)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 3);
        }
        else if (property == char.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
            mv.visitMethodInsn(INVOKEVIRTUAL, parent.getInternalName(), method, "(C)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == double.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
            mv.visitMethodInsn(INVOKEVIRTUAL, parent.getInternalName(), method, "(D)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 3);
        }
        else if (property == float.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
            mv.visitMethodInsn(INVOKEVIRTUAL, parent.getInternalName(), method, "(F)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == short.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
            mv.visitMethodInsn(INVOKEVIRTUAL, parent.getInternalName(), method, "(S)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == byte.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
            mv.visitMethodInsn(INVOKEVIRTUAL, parent.getInternalName(), method, "(B)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
    }
    
    private void invokeSetField(MethodVisitor mv, String field, Type parent, Class<?> property) {
        
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, parent.getInternalName());
        mv.visitVarInsn(ALOAD, 2);
        
        if (!property.isPrimitive()) {
            Type type = Type.getType(property);
            
            mv.visitTypeInsn(CHECKCAST, type.getInternalName());
            mv.visitFieldInsn(PUTFIELD, parent.getInternalName(), field, 
                type.getReturnType().toString());                
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        
        else if (property == boolean.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
            mv.visitFieldInsn(PUTFIELD, parent.getInternalName(), field, "Z");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == int.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
            mv.visitFieldInsn(PUTFIELD, parent.getInternalName(), field, "I");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == long.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
            mv.visitFieldInsn(PUTFIELD, parent.getInternalName(), field, "J");
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 3);
        }
        else if (property == char.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
            mv.visitFieldInsn(PUTFIELD, parent.getInternalName(), field, "C");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == double.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
            mv.visitFieldInsn(PUTFIELD, parent.getInternalName(), field, "D");
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 3);
        }
        else if (property == float.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
            mv.visitFieldInsn(PUTFIELD, parent.getInternalName(), field, "F");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == short.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
            mv.visitFieldInsn(PUTFIELD, parent.getInternalName(), field, "S");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
        else if (property == byte.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
            mv.visitFieldInsn(PUTFIELD, parent.getInternalName(), field, "B");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
        }
    }
    
    /**
     * Attempts to load the {@link PropertyAccessorSupport} implementation
     * of the given name.
     * @param name the class name
     * @return a new {@link PropertyAccessorSupport} or null if the class name
     *   was not found
     */
    protected PropertyAccessorSupport createAccessor(String name) {
        try {
            return createAccessor(classLoader.loadClass(name));
        }
        catch (ClassNotFoundException ex) {
            return null;
        }
    }
    
    protected PropertyAccessorSupport createAccessor(Class<?> supportClass) {
        try {
            return (PropertyAccessorSupport) supportClass.newInstance();
        }
        // thrown if class is not a PropertyAccessorSupport...
        catch (Exception e) {
            throw new BeanIOException("ASM accessor factory failed to " +
                "instantiate class '" + supportClass.getName() + "'", e);
        }
    }

    private static final class AsmClassLoader extends ClassLoader {
        public AsmClassLoader(ClassLoader parent) {
            super(parent);
        }
        
        public Class<?> defineClass(String name, byte[] b) throws ClassFormatError {
            return super.defineClass(name, b, 0, b.length);
        }
    };
}
