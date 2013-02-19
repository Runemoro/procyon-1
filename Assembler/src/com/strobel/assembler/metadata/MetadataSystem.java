/*
 * MetadataSystem.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.ClassFileReader;
import com.strobel.core.Fences;
import com.strobel.core.VerifyArgument;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mike Strobel
 */
public class MetadataSystem extends MetadataResolver {
    private static MetadataSystem _instance;

    private final ConcurrentHashMap<String, TypeDefinition> _types;
    private final ITypeLoader _typeLoader;

    public static MetadataSystem instance() {
        if (_instance == null) {
            synchronized (MetadataSystem.class) {
                if (_instance == null) {
                    _instance = Fences.orderWrites(new MetadataSystem());
                }
            }
        }
        return _instance;
    }

    public MetadataSystem() {
        this(System.getProperty("java.class.path"));
    }

    public MetadataSystem(final String classPath) {
        this(new ClasspathTypeLoader(VerifyArgument.notNull(classPath, "classPath")));
    }

    public MetadataSystem(final ITypeLoader typeLoader) {
        _typeLoader = VerifyArgument.notNull(typeLoader, "typeLoader");
        _types = new ConcurrentHashMap<>();
    }

    public void addTypeDefinition(final TypeDefinition type) {
        VerifyArgument.notNull(type, "type");
        _types.putIfAbsent(type.getInternalName(), type);
    }

    @Override
    protected TypeDefinition resolveCore(final TypeReference type) {
        VerifyArgument.notNull(type, "type");
        return resolveType(type.getInternalName());
    }

    @Override
    protected TypeReference lookupTypeCore(final String descriptor) {
        return resolveType(descriptor);
    }

    protected TypeDefinition resolveType(final String descriptor) {
        VerifyArgument.notNull(descriptor, "descriptor");

        if (descriptor.length() == 1) {
            final int primitiveHash = descriptor.charAt(0) - 'B';
            final TypeDefinition primitiveType = PRIMITIVE_TYPES_BY_DESCRIPTOR[primitiveHash];

            if (primitiveType != null) {
                return primitiveType;
            }
        }
        else {
            final int primitiveHash = hashPrimitiveName(descriptor);
            final TypeDefinition primitiveType = PRIMITIVE_TYPES_BY_NAME[primitiveHash];

            if (primitiveType != null && descriptor.equals(primitiveType.getName())) {
                return primitiveType;
            }
        }

        TypeDefinition cachedDefinition = _types.get(descriptor);

        if (cachedDefinition != null) {
            return cachedDefinition;
        }

        final Buffer buffer = new Buffer(0);

        if (!_typeLoader.tryLoadType(descriptor, buffer)) {
            return null;
        }

        final ClassFileReader reader = ClassFileReader.readClass(this, buffer);
        final TypeDefinitionBuilder builder = new TypeDefinitionBuilder();

        reader.accept(builder);

        final TypeDefinition typeDefinition = builder.getTypeDefinition();

        cachedDefinition = _types.putIfAbsent(descriptor, typeDefinition);

        if (cachedDefinition != null) {
            return cachedDefinition;
        }

        return typeDefinition;
    }

    // <editor-fold defaultstate="collapsed" desc="Primitive Lookup">

    private final static TypeDefinition[] PRIMITIVE_TYPES_BY_NAME = new TypeDefinition['Z' - 'B' + 1];
    private final static TypeDefinition[] PRIMITIVE_TYPES_BY_DESCRIPTOR = new TypeDefinition[16];

    static {
        final TypeDefinition[] allPrimitives = {
            BuiltinTypes.Boolean,
            BuiltinTypes.Byte,
            BuiltinTypes.Character,
            BuiltinTypes.Short,
            BuiltinTypes.Integer,
            BuiltinTypes.Long,
            BuiltinTypes.Float,
            BuiltinTypes.Double,
            BuiltinTypes.Void
        };

        for (final TypeDefinition t : allPrimitives) {
            PRIMITIVE_TYPES_BY_DESCRIPTOR[hashPrimitiveName(t.getName())] = t;
            PRIMITIVE_TYPES_BY_NAME[t.getInternalName().charAt(0) - 'B'] = t;
        }
    }

    private static int hashPrimitiveName(final String name) {
        if (name.length() < 3) {
            return 0;
        }
        return (name.charAt(0) + name.charAt(2)) % 16;
    }

    // </editor-fold>
}
