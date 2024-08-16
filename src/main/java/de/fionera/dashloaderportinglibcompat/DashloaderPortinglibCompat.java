package de.fionera.dashloaderportinglibcompat;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public class DashloaderPortinglibCompat implements Runnable {
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void run() {
        LOGGER.info("Injecting additional constructor into ItemTransform class");
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

        String itemTransform = remapper.mapClassName("intermediary", "net.minecraft.class_804");
        ClassTinkerers.addTransformation(itemTransform, target -> {
            MethodNode origConstructor = target.methods.getFirst();

            MethodNode newConstructor = new MethodNode(
                    Opcodes.ASM7,
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    "(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Lorg/joml/Vector3f;Lorg/joml/Vector3f;)V",
                    null,
                    null
            );

            newConstructor.parameters = new ArrayList<>(origConstructor.parameters);
            newConstructor.parameters.add(new ParameterNode("vector3f", 0));

            newConstructor.instructions = new InsnList();
            origConstructor.instructions.accept(newConstructor);

            AbstractInsnNode endLabelInstr = newConstructor.instructions.getLast();
            newConstructor.instructions.remove(endLabelInstr);

            AbstractInsnNode returnIntr = newConstructor.instructions.getLast();
            newConstructor.instructions.remove(returnIntr);

            newConstructor.instructions.add(
                    new VarInsnNode(Opcodes.ALOAD, 0)
            );

            newConstructor.instructions.add(
                    new TypeInsnNode(Opcodes.NEW, "org/joml/Vector3f")
            );

            newConstructor.instructions.add(
                    new InsnNode(Opcodes.DUP)
            );

            newConstructor.instructions.add(
                    new VarInsnNode(Opcodes.ALOAD, 4)
            );

            newConstructor.instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKESPECIAL,
                            "org/joml/Vector3f",
                            "<init>",
                            "(Lorg/joml/Vector3fc;)V"
                    )
            );

            newConstructor.instructions.add(
                    new FieldInsnNode(
                            Opcodes.PUTFIELD,
                            target.name,
                            "rightRotation",
                            "Lorg/joml/Vector3f;"
                    )
            );

            newConstructor.instructions.add(returnIntr);
            newConstructor.instructions.add(endLabelInstr);

            newConstructor.localVariables = new ArrayList<>(origConstructor.localVariables);
            newConstructor.localVariables.add(new LocalVariableNode(
                    "vector3f4",
                    "Lorg/joml/Vector3f;",
                    null,
                    (LabelNode) newConstructor.instructions.getFirst(),
                    (LabelNode) newConstructor.instructions.getLast(),
                    4
            ));

            target.methods.addFirst(newConstructor);
        });
    }
}
