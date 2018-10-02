package android.support.constraint.solver.widgets;

import android.support.constraint.solver.ArrayRow;
import android.support.constraint.solver.LinearSystem;
import android.support.constraint.solver.SolverVariable;
import android.support.constraint.solver.widgets.ConstraintAnchor.Type;
import android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour;
import java.util.ArrayList;
import java.util.Arrays;

public class ConstraintWidgetContainer extends WidgetContainer {
    static boolean ALLOW_ROOT_GROUP = true;
    private static final int CHAIN_FIRST = 0;
    private static final int CHAIN_FIRST_VISIBLE = 2;
    private static final int CHAIN_LAST = 1;
    private static final int CHAIN_LAST_VISIBLE = 3;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_LAYOUT = false;
    private static final boolean DEBUG_OPTIMIZE = false;
    private static final int FLAG_CHAIN_DANGLING = 1;
    private static final int FLAG_CHAIN_OPTIMIZE = 0;
    private static final int FLAG_RECOMPUTE_BOUNDS = 2;
    private static final int MAX_ITERATIONS = 8;
    public static final int OPTIMIZATION_ALL = 2;
    public static final int OPTIMIZATION_BASIC = 4;
    public static final int OPTIMIZATION_CHAIN = 8;
    public static final int OPTIMIZATION_NONE = 1;
    private static final boolean USE_SNAPSHOT = true;
    private static final boolean USE_THREAD = false;
    private boolean[] flags;
    protected LinearSystem mBackgroundSystem;
    private ConstraintWidget[] mChainEnds;
    private boolean mHeightMeasuredTooSmall;
    private ConstraintWidget[] mHorizontalChainsArray;
    private int mHorizontalChainsSize;
    private ConstraintWidget[] mMatchConstraintsChainedWidgets;
    private int mOptimizationLevel;
    int mPaddingBottom;
    int mPaddingLeft;
    int mPaddingRight;
    int mPaddingTop;
    private Snapshot mSnapshot;
    protected LinearSystem mSystem;
    private ConstraintWidget[] mVerticalChainsArray;
    private int mVerticalChainsSize;
    private boolean mWidthMeasuredTooSmall;
    int mWrapHeight;
    int mWrapWidth;

    public String getType() {
        return "ConstraintLayout";
    }

    public boolean handlesInternalConstraints() {
        return false;
    }

    public ConstraintWidgetContainer() {
        this.mSystem = new LinearSystem();
        this.mBackgroundSystem = null;
        this.mHorizontalChainsSize = 0;
        this.mVerticalChainsSize = 0;
        this.mMatchConstraintsChainedWidgets = new ConstraintWidget[4];
        this.mVerticalChainsArray = new ConstraintWidget[4];
        this.mHorizontalChainsArray = new ConstraintWidget[4];
        this.mOptimizationLevel = 2;
        this.flags = new boolean[3];
        this.mChainEnds = new ConstraintWidget[4];
        this.mWidthMeasuredTooSmall = false;
        this.mHeightMeasuredTooSmall = false;
    }

    public ConstraintWidgetContainer(int i, int i2, int i3, int i4) {
        super(i, i2, i3, i4);
        this.mSystem = new LinearSystem();
        this.mBackgroundSystem = 0;
        this.mHorizontalChainsSize = 0;
        this.mVerticalChainsSize = 0;
        this.mMatchConstraintsChainedWidgets = new ConstraintWidget[4];
        this.mVerticalChainsArray = new ConstraintWidget[4];
        this.mHorizontalChainsArray = new ConstraintWidget[4];
        this.mOptimizationLevel = 2;
        this.flags = new boolean[3];
        this.mChainEnds = new ConstraintWidget[4];
        this.mWidthMeasuredTooSmall = false;
        this.mHeightMeasuredTooSmall = false;
    }

    public ConstraintWidgetContainer(int i, int i2) {
        super(i, i2);
        this.mSystem = new LinearSystem();
        this.mBackgroundSystem = 0;
        this.mHorizontalChainsSize = 0;
        this.mVerticalChainsSize = 0;
        this.mMatchConstraintsChainedWidgets = new ConstraintWidget[4];
        this.mVerticalChainsArray = new ConstraintWidget[4];
        this.mHorizontalChainsArray = new ConstraintWidget[4];
        this.mOptimizationLevel = 2;
        this.flags = new boolean[3];
        this.mChainEnds = new ConstraintWidget[4];
        this.mWidthMeasuredTooSmall = false;
        this.mHeightMeasuredTooSmall = false;
    }

    public void setOptimizationLevel(int i) {
        this.mOptimizationLevel = i;
    }

    public void reset() {
        this.mSystem.reset();
        this.mPaddingLeft = 0;
        this.mPaddingRight = 0;
        this.mPaddingTop = 0;
        this.mPaddingBottom = 0;
        super.reset();
    }

    public boolean isWidthMeasuredTooSmall() {
        return this.mWidthMeasuredTooSmall;
    }

    public boolean isHeightMeasuredTooSmall() {
        return this.mHeightMeasuredTooSmall;
    }

    public static ConstraintWidgetContainer createContainer(ConstraintWidgetContainer constraintWidgetContainer, String str, ArrayList<ConstraintWidget> arrayList, int i) {
        Rectangle bounds = WidgetContainer.getBounds(arrayList);
        if (bounds.width != 0) {
            if (bounds.height != 0) {
                int min;
                if (i > 0) {
                    min = Math.min(bounds.f4x, bounds.f5y);
                    if (i > min) {
                        i = min;
                    }
                    bounds.grow(i, i);
                }
                constraintWidgetContainer.setOrigin(bounds.f4x, bounds.f5y);
                constraintWidgetContainer.setDimension(bounds.width, bounds.height);
                constraintWidgetContainer.setDebugName(str);
                str = null;
                i = ((ConstraintWidget) arrayList.get(0)).getParent();
                min = arrayList.size();
                while (str < min) {
                    ConstraintWidget constraintWidget = (ConstraintWidget) arrayList.get(str);
                    if (constraintWidget.getParent() == i) {
                        constraintWidgetContainer.add(constraintWidget);
                        constraintWidget.setX(constraintWidget.getX() - bounds.f4x);
                        constraintWidget.setY(constraintWidget.getY() - bounds.f5y);
                    }
                    str++;
                }
                return constraintWidgetContainer;
            }
        }
        return null;
    }

    public boolean addChildrenToSolver(LinearSystem linearSystem, int i) {
        boolean z;
        ConstraintWidget constraintWidget;
        DimensionBehaviour dimensionBehaviour;
        DimensionBehaviour dimensionBehaviour2;
        addToSolver(linearSystem, i);
        int size = this.mChildren.size();
        int i2 = 0;
        if (this.mOptimizationLevel != 2) {
            if (this.mOptimizationLevel != 4) {
                z = true;
                while (i2 < size) {
                    constraintWidget = (ConstraintWidget) this.mChildren.get(i2);
                    if (constraintWidget instanceof ConstraintWidgetContainer) {
                        if (z) {
                            Optimizer.checkMatchParent(this, linearSystem, constraintWidget);
                        }
                        constraintWidget.addToSolver(linearSystem, i);
                    } else {
                        dimensionBehaviour = constraintWidget.mHorizontalDimensionBehaviour;
                        dimensionBehaviour2 = constraintWidget.mVerticalDimensionBehaviour;
                        if (dimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                            constraintWidget.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED);
                        }
                        if (dimensionBehaviour2 == DimensionBehaviour.WRAP_CONTENT) {
                            constraintWidget.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED);
                        }
                        constraintWidget.addToSolver(linearSystem, i);
                        if (dimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                            constraintWidget.setHorizontalDimensionBehaviour(dimensionBehaviour);
                        }
                        if (dimensionBehaviour2 == DimensionBehaviour.WRAP_CONTENT) {
                            constraintWidget.setVerticalDimensionBehaviour(dimensionBehaviour2);
                        }
                    }
                    i2++;
                }
                if (this.mHorizontalChainsSize > 0) {
                    applyHorizontalChain(linearSystem);
                }
                if (this.mVerticalChainsSize > 0) {
                    applyVerticalChain(linearSystem);
                }
                return true;
            }
        }
        if (optimize(linearSystem)) {
            return false;
        }
        z = false;
        while (i2 < size) {
            constraintWidget = (ConstraintWidget) this.mChildren.get(i2);
            if (constraintWidget instanceof ConstraintWidgetContainer) {
                if (z) {
                    Optimizer.checkMatchParent(this, linearSystem, constraintWidget);
                }
                constraintWidget.addToSolver(linearSystem, i);
            } else {
                dimensionBehaviour = constraintWidget.mHorizontalDimensionBehaviour;
                dimensionBehaviour2 = constraintWidget.mVerticalDimensionBehaviour;
                if (dimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                    constraintWidget.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED);
                }
                if (dimensionBehaviour2 == DimensionBehaviour.WRAP_CONTENT) {
                    constraintWidget.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED);
                }
                constraintWidget.addToSolver(linearSystem, i);
                if (dimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                    constraintWidget.setHorizontalDimensionBehaviour(dimensionBehaviour);
                }
                if (dimensionBehaviour2 == DimensionBehaviour.WRAP_CONTENT) {
                    constraintWidget.setVerticalDimensionBehaviour(dimensionBehaviour2);
                }
            }
            i2++;
        }
        if (this.mHorizontalChainsSize > 0) {
            applyHorizontalChain(linearSystem);
        }
        if (this.mVerticalChainsSize > 0) {
            applyVerticalChain(linearSystem);
        }
        return true;
    }

    private boolean optimize(LinearSystem linearSystem) {
        int i;
        int size = this.mChildren.size();
        for (i = 0; i < size; i++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i);
            constraintWidget.mHorizontalResolution = -1;
            constraintWidget.mVerticalResolution = -1;
            if (constraintWidget.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT || constraintWidget.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                constraintWidget.mHorizontalResolution = 1;
                constraintWidget.mVerticalResolution = 1;
            }
        }
        boolean z = false;
        boolean z2 = z;
        boolean z3 = z2;
        while (!z) {
            int i2 = 0;
            boolean z4 = i2;
            boolean z5 = z4;
            while (i2 < size) {
                ConstraintWidget constraintWidget2 = (ConstraintWidget) this.mChildren.get(i2);
                if (constraintWidget2.mHorizontalResolution == -1) {
                    if (this.mHorizontalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                        constraintWidget2.mHorizontalResolution = 1;
                    } else {
                        Optimizer.checkHorizontalSimpleDependency(this, linearSystem, constraintWidget2);
                    }
                }
                if (constraintWidget2.mVerticalResolution == -1) {
                    if (this.mVerticalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                        constraintWidget2.mVerticalResolution = 1;
                    } else {
                        Optimizer.checkVerticalSimpleDependency(this, linearSystem, constraintWidget2);
                    }
                }
                if (constraintWidget2.mVerticalResolution == -1) {
                    z4++;
                }
                if (constraintWidget2.mHorizontalResolution == -1) {
                    z5++;
                }
                i2++;
            }
            if (z4 || z5) {
                if (z2 == z4 && r6 == z5) {
                }
                z2 = z4;
                z3 = z5;
            }
            z = true;
            z2 = z4;
            z3 = z5;
        }
        linearSystem = null;
        i = linearSystem;
        int i3 = i;
        while (linearSystem < size) {
            ConstraintWidget constraintWidget3 = (ConstraintWidget) this.mChildren.get(linearSystem);
            if (constraintWidget3.mHorizontalResolution == 1 || constraintWidget3.mHorizontalResolution == -1) {
                i++;
            }
            if (constraintWidget3.mVerticalResolution == 1 || constraintWidget3.mVerticalResolution == -1) {
                i3++;
            }
            linearSystem++;
        }
        return i == 0 && i3 == 0;
    }

    private void applyHorizontalChain(LinearSystem linearSystem) {
        ConstraintWidgetContainer constraintWidgetContainer = this;
        LinearSystem linearSystem2 = linearSystem;
        int i = 0;
        int i2 = 0;
        while (i2 < constraintWidgetContainer.mHorizontalChainsSize) {
            int margin;
            LinearSystem linearSystem3;
            int i3;
            ConstraintWidget constraintWidget = constraintWidgetContainer.mHorizontalChainsArray[i2];
            int countMatchConstraintsChainedWidgets = constraintWidgetContainer.countMatchConstraintsChainedWidgets(linearSystem2, constraintWidgetContainer.mChainEnds, constraintWidgetContainer.mHorizontalChainsArray[i2], 0, constraintWidgetContainer.flags);
            ConstraintWidget constraintWidget2 = constraintWidgetContainer.mChainEnds[2];
            if (constraintWidget2 != null) {
                if (constraintWidgetContainer.flags[1]) {
                    countMatchConstraintsChainedWidgets = constraintWidget.getDrawX();
                    while (constraintWidget2 != null) {
                        linearSystem2.addEquality(constraintWidget2.mLeft.mSolverVariable, countMatchConstraintsChainedWidgets);
                        countMatchConstraintsChainedWidgets += (constraintWidget2.mLeft.getMargin() + constraintWidget2.getWidth()) + constraintWidget2.mRight.getMargin();
                        constraintWidget2 = constraintWidget2.mHorizontalNextWidget;
                    }
                } else {
                    int i4 = constraintWidget.mHorizontalChainStyle == 0 ? 1 : i;
                    int i5 = constraintWidget.mHorizontalChainStyle == 2 ? 1 : i;
                    int i6 = constraintWidgetContainer.mHorizontalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT ? 1 : i;
                    if ((constraintWidgetContainer.mOptimizationLevel == 2 || constraintWidgetContainer.mOptimizationLevel == 8) && constraintWidgetContainer.flags[i] && constraintWidget.mHorizontalChainFixedPosition && i5 == 0 && i6 == 0 && constraintWidget.mHorizontalChainStyle == 0) {
                        Optimizer.applyDirectResolutionHorizontalChain(constraintWidgetContainer, linearSystem2, countMatchConstraintsChainedWidgets, constraintWidget);
                    } else {
                        ConstraintWidget constraintWidget3;
                        ConstraintWidget constraintWidget4;
                        SolverVariable solverVariable;
                        ConstraintWidget constraintWidget5;
                        int margin2;
                        SolverVariable solverVariable2;
                        int i7 = 3;
                        if (countMatchConstraintsChainedWidgets != 0) {
                            if (i5 == 0) {
                                float f = 0.0f;
                                constraintWidget3 = null;
                                while (constraintWidget2 != null) {
                                    if (constraintWidget2.mHorizontalDimensionBehaviour != DimensionBehaviour.MATCH_CONSTRAINT) {
                                        i6 = constraintWidget2.mLeft.getMargin();
                                        if (constraintWidget3 != null) {
                                            i6 += constraintWidget3.mRight.getMargin();
                                        }
                                        linearSystem2.addGreaterThan(constraintWidget2.mLeft.mSolverVariable, constraintWidget2.mLeft.mTarget.mSolverVariable, i6, constraintWidget2.mLeft.mTarget.mOwner.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT ? 2 : 3);
                                        i4 = constraintWidget2.mRight.getMargin();
                                        if (constraintWidget2.mRight.mTarget.mOwner.mLeft.mTarget != null && constraintWidget2.mRight.mTarget.mOwner.mLeft.mTarget.mOwner == constraintWidget2) {
                                            i4 += constraintWidget2.mRight.mTarget.mOwner.mLeft.getMargin();
                                        }
                                        linearSystem2.addLowerThan(constraintWidget2.mRight.mSolverVariable, constraintWidget2.mRight.mTarget.mSolverVariable, -i4, constraintWidget2.mRight.mTarget.mOwner.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT ? 2 : 3);
                                    } else {
                                        f += constraintWidget2.mHorizontalWeight;
                                        if (constraintWidget2.mRight.mTarget != null) {
                                            i4 = constraintWidget2.mRight.getMargin();
                                            if (constraintWidget2 != constraintWidgetContainer.mChainEnds[3]) {
                                                i4 += constraintWidget2.mRight.mTarget.mOwner.mLeft.getMargin();
                                            }
                                        } else {
                                            i4 = i;
                                        }
                                        linearSystem2.addGreaterThan(constraintWidget2.mRight.mSolverVariable, constraintWidget2.mLeft.mSolverVariable, i, 1);
                                        linearSystem2.addLowerThan(constraintWidget2.mRight.mSolverVariable, constraintWidget2.mRight.mTarget.mSolverVariable, -i4, 1);
                                    }
                                    constraintWidget3 = constraintWidget2;
                                    constraintWidget2 = constraintWidget2.mHorizontalNextWidget;
                                }
                                int i8;
                                if (countMatchConstraintsChainedWidgets != 1) {
                                    i8 = i;
                                    while (true) {
                                        i4 = countMatchConstraintsChainedWidgets - 1;
                                        if (i8 >= i4) {
                                            break;
                                        }
                                        ConstraintWidget constraintWidget6 = constraintWidgetContainer.mMatchConstraintsChainedWidgets[i8];
                                        i8++;
                                        constraintWidget4 = constraintWidgetContainer.mMatchConstraintsChainedWidgets[i8];
                                        solverVariable = constraintWidget6.mLeft.mSolverVariable;
                                        SolverVariable solverVariable3 = constraintWidget6.mRight.mSolverVariable;
                                        SolverVariable solverVariable4 = constraintWidget4.mLeft.mSolverVariable;
                                        SolverVariable solverVariable5 = constraintWidget4.mRight.mSolverVariable;
                                        if (constraintWidget4 == constraintWidgetContainer.mChainEnds[i7]) {
                                            solverVariable5 = constraintWidgetContainer.mChainEnds[1].mRight.mSolverVariable;
                                        }
                                        margin = constraintWidget6.mLeft.getMargin();
                                        if (!(constraintWidget6.mLeft.mTarget == null || constraintWidget6.mLeft.mTarget.mOwner.mRight.mTarget == null || constraintWidget6.mLeft.mTarget.mOwner.mRight.mTarget.mOwner != constraintWidget6)) {
                                            margin += constraintWidget6.mLeft.mTarget.mOwner.mRight.getMargin();
                                        }
                                        int i9 = countMatchConstraintsChainedWidgets;
                                        linearSystem2.addGreaterThan(solverVariable, constraintWidget6.mLeft.mTarget.mSolverVariable, margin, 2);
                                        countMatchConstraintsChainedWidgets = constraintWidget6.mRight.getMargin();
                                        if (!(constraintWidget6.mRight.mTarget == null || constraintWidget6.mHorizontalNextWidget == null)) {
                                            countMatchConstraintsChainedWidgets += constraintWidget6.mHorizontalNextWidget.mLeft.mTarget != null ? constraintWidget6.mHorizontalNextWidget.mLeft.getMargin() : 0;
                                        }
                                        linearSystem2.addLowerThan(solverVariable3, constraintWidget6.mRight.mTarget.mSolverVariable, -countMatchConstraintsChainedWidgets, 2);
                                        if (i8 == i4) {
                                            countMatchConstraintsChainedWidgets = constraintWidget4.mLeft.getMargin();
                                            if (!(constraintWidget4.mLeft.mTarget == null || constraintWidget4.mLeft.mTarget.mOwner.mRight.mTarget == null || constraintWidget4.mLeft.mTarget.mOwner.mRight.mTarget.mOwner != constraintWidget4)) {
                                                countMatchConstraintsChainedWidgets += constraintWidget4.mLeft.mTarget.mOwner.mRight.getMargin();
                                            }
                                            linearSystem2.addGreaterThan(solverVariable4, constraintWidget4.mLeft.mTarget.mSolverVariable, countMatchConstraintsChainedWidgets, 2);
                                            ConstraintAnchor constraintAnchor = constraintWidget4.mRight;
                                            if (constraintWidget4 == constraintWidgetContainer.mChainEnds[3]) {
                                                constraintAnchor = constraintWidgetContainer.mChainEnds[1].mRight;
                                            }
                                            i4 = constraintAnchor.getMargin();
                                            if (!(constraintAnchor.mTarget == null || constraintAnchor.mTarget.mOwner.mLeft.mTarget == null || constraintAnchor.mTarget.mOwner.mLeft.mTarget.mOwner != constraintWidget4)) {
                                                i4 += constraintAnchor.mTarget.mOwner.mLeft.getMargin();
                                            }
                                            margin = 2;
                                            linearSystem2.addLowerThan(solverVariable5, constraintAnchor.mTarget.mSolverVariable, -i4, 2);
                                        } else {
                                            margin = 2;
                                        }
                                        if (constraintWidget.mMatchConstraintMaxWidth > 0) {
                                            linearSystem2.addLowerThan(solverVariable3, solverVariable, constraintWidget.mMatchConstraintMaxWidth, margin);
                                        }
                                        ArrayRow createRow = linearSystem.createRow();
                                        createRow.createRowEqualDimension(constraintWidget6.mHorizontalWeight, f, constraintWidget4.mHorizontalWeight, solverVariable, constraintWidget6.mLeft.getMargin(), solverVariable3, constraintWidget6.mRight.getMargin(), solverVariable4, constraintWidget4.mLeft.getMargin(), solverVariable5, constraintWidget4.mRight.getMargin());
                                        linearSystem2.addConstraint(createRow);
                                        countMatchConstraintsChainedWidgets = i9;
                                        i7 = 3;
                                        i = 0;
                                    }
                                } else {
                                    constraintWidget5 = constraintWidgetContainer.mMatchConstraintsChainedWidgets[i];
                                    i8 = constraintWidget5.mLeft.getMargin();
                                    if (constraintWidget5.mLeft.mTarget != null) {
                                        i8 += constraintWidget5.mLeft.mTarget.getMargin();
                                    }
                                    i4 = constraintWidget5.mRight.getMargin();
                                    if (constraintWidget5.mRight.mTarget != null) {
                                        i4 += constraintWidget5.mRight.mTarget.getMargin();
                                    }
                                    SolverVariable solverVariable6 = constraintWidget.mRight.mTarget.mSolverVariable;
                                    if (constraintWidget5 == constraintWidgetContainer.mChainEnds[3]) {
                                        solverVariable6 = constraintWidgetContainer.mChainEnds[1].mRight.mTarget.mSolverVariable;
                                    }
                                    if (constraintWidget5.mMatchConstraintDefaultWidth == 1) {
                                        linearSystem2.addGreaterThan(constraintWidget.mLeft.mSolverVariable, constraintWidget.mLeft.mTarget.mSolverVariable, i8, 1);
                                        linearSystem2.addLowerThan(constraintWidget.mRight.mSolverVariable, solverVariable6, -i4, 1);
                                        linearSystem2.addEquality(constraintWidget.mRight.mSolverVariable, constraintWidget.mLeft.mSolverVariable, constraintWidget.getWidth(), 2);
                                    } else {
                                        linearSystem2.addEquality(constraintWidget5.mLeft.mSolverVariable, constraintWidget5.mLeft.mTarget.mSolverVariable, i8, 1);
                                        linearSystem2.addEquality(constraintWidget5.mRight.mSolverVariable, solverVariable6, -i4, 1);
                                    }
                                }
                            }
                        }
                        constraintWidget5 = constraintWidget2;
                        ConstraintWidget constraintWidget7 = null;
                        ConstraintWidget constraintWidget8 = constraintWidget7;
                        Object obj = null;
                        while (constraintWidget5 != null) {
                            ConstraintWidget constraintWidget9;
                            Object obj2;
                            ConstraintWidget constraintWidget10;
                            int i10;
                            ConstraintWidget constraintWidget11;
                            constraintWidget4 = constraintWidget5.mHorizontalNextWidget;
                            if (constraintWidget4 == null) {
                                constraintWidget9 = constraintWidgetContainer.mChainEnds[1];
                                obj2 = 1;
                            } else {
                                constraintWidget9 = constraintWidget7;
                                obj2 = obj;
                            }
                            ConstraintAnchor constraintAnchor2;
                            int margin3;
                            if (i5 != 0) {
                                constraintAnchor2 = constraintWidget5.mLeft;
                                margin3 = constraintAnchor2.getMargin();
                                if (constraintWidget8 != null) {
                                    margin3 += constraintWidget8.mRight.getMargin();
                                }
                                linearSystem2.addGreaterThan(constraintAnchor2.mSolverVariable, constraintAnchor2.mTarget.mSolverVariable, margin3, constraintWidget2 != constraintWidget5 ? 3 : 1);
                                if (constraintWidget5.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                                    ConstraintAnchor constraintAnchor3 = constraintWidget5.mRight;
                                    if (constraintWidget5.mMatchConstraintDefaultWidth == 1) {
                                        linearSystem2.addEquality(constraintAnchor3.mSolverVariable, constraintAnchor2.mSolverVariable, Math.max(constraintWidget5.mMatchConstraintMinWidth, constraintWidget5.getWidth()), 3);
                                    } else {
                                        linearSystem2.addGreaterThan(constraintAnchor2.mSolverVariable, constraintAnchor2.mTarget.mSolverVariable, constraintAnchor2.mMargin, 3);
                                        linearSystem2.addLowerThan(constraintAnchor3.mSolverVariable, constraintAnchor2.mSolverVariable, constraintWidget5.mMatchConstraintMinWidth, 3);
                                    }
                                }
                            } else if (i4 != 0 || obj2 == null || constraintWidget8 == null) {
                                if (i4 != 0 || obj2 != null || constraintWidget8 != null) {
                                    ConstraintWidget constraintWidget12;
                                    ConstraintAnchor constraintAnchor4 = constraintWidget5.mLeft;
                                    constraintAnchor2 = constraintWidget5.mRight;
                                    margin2 = constraintAnchor4.getMargin();
                                    margin3 = constraintAnchor2.getMargin();
                                    constraintWidget10 = constraintWidget5;
                                    i10 = i4;
                                    linearSystem2.addGreaterThan(constraintAnchor4.mSolverVariable, constraintAnchor4.mTarget.mSolverVariable, margin2, 1);
                                    int i11 = i2;
                                    linearSystem2.addLowerThan(constraintAnchor2.mSolverVariable, constraintAnchor2.mTarget.mSolverVariable, -margin3, 1);
                                    SolverVariable solverVariable7 = constraintAnchor4.mTarget != null ? constraintAnchor4.mTarget.mSolverVariable : null;
                                    if (constraintWidget8 == null) {
                                        solverVariable7 = constraintWidget.mLeft.mTarget != null ? constraintWidget.mLeft.mTarget.mSolverVariable : null;
                                    }
                                    if (constraintWidget4 == null) {
                                        constraintWidget4 = constraintWidget9.mRight.mTarget != null ? constraintWidget9.mRight.mTarget.mOwner : null;
                                    }
                                    constraintWidget3 = constraintWidget4;
                                    if (constraintWidget3 != null) {
                                        SolverVariable solverVariable8 = constraintWidget3.mLeft.mSolverVariable;
                                        if (obj2 != null) {
                                            solverVariable8 = constraintWidget9.mRight.mTarget != null ? constraintWidget9.mRight.mTarget.mSolverVariable : null;
                                        }
                                        if (!(solverVariable7 == null || solverVariable8 == null)) {
                                            int i12 = margin3;
                                            solverVariable = solverVariable7;
                                            constraintWidget5 = constraintWidget;
                                            solverVariable2 = solverVariable8;
                                            margin = i11;
                                            constraintWidget11 = constraintWidget9;
                                            constraintWidget12 = constraintWidget3;
                                            linearSystem3 = linearSystem2;
                                            linearSystem2.addCentering(constraintAnchor4.mSolverVariable, solverVariable, margin2, 0.5f, solverVariable2, constraintAnchor2.mSolverVariable, i12, 4);
                                            constraintWidget4 = constraintWidget12;
                                            if (obj2 == null) {
                                                constraintWidget4 = null;
                                            }
                                            constraintWidget = constraintWidget5;
                                            obj = obj2;
                                            linearSystem2 = linearSystem3;
                                            i2 = margin;
                                            constraintWidget5 = constraintWidget4;
                                            constraintWidget7 = constraintWidget11;
                                            constraintWidget8 = constraintWidget10;
                                            i4 = i10;
                                            constraintWidgetContainer = this;
                                        }
                                    }
                                    constraintWidget12 = constraintWidget3;
                                    constraintWidget5 = constraintWidget;
                                    constraintWidget11 = constraintWidget9;
                                    linearSystem3 = linearSystem2;
                                    margin = i11;
                                    constraintWidget4 = constraintWidget12;
                                    if (obj2 == null) {
                                        constraintWidget4 = null;
                                    }
                                    constraintWidget = constraintWidget5;
                                    obj = obj2;
                                    linearSystem2 = linearSystem3;
                                    i2 = margin;
                                    constraintWidget5 = constraintWidget4;
                                    constraintWidget7 = constraintWidget11;
                                    constraintWidget8 = constraintWidget10;
                                    i4 = i10;
                                    constraintWidgetContainer = this;
                                } else if (constraintWidget5.mLeft.mTarget == null) {
                                    linearSystem2.addEquality(constraintWidget5.mLeft.mSolverVariable, constraintWidget5.getDrawX());
                                } else {
                                    linearSystem2.addEquality(constraintWidget5.mLeft.mSolverVariable, constraintWidget.mLeft.mTarget.mSolverVariable, constraintWidget5.mLeft.getMargin(), 5);
                                }
                            } else if (constraintWidget5.mRight.mTarget == null) {
                                linearSystem2.addEquality(constraintWidget5.mRight.mSolverVariable, constraintWidget5.getDrawRight());
                            } else {
                                linearSystem2.addEquality(constraintWidget5.mRight.mSolverVariable, constraintWidget9.mRight.mTarget.mSolverVariable, -constraintWidget5.mRight.getMargin(), 5);
                            }
                            constraintWidget10 = constraintWidget5;
                            i10 = i4;
                            constraintWidget5 = constraintWidget;
                            margin = i2;
                            constraintWidget11 = constraintWidget9;
                            linearSystem3 = linearSystem2;
                            if (obj2 == null) {
                                constraintWidget4 = null;
                            }
                            constraintWidget = constraintWidget5;
                            obj = obj2;
                            linearSystem2 = linearSystem3;
                            i2 = margin;
                            constraintWidget5 = constraintWidget4;
                            constraintWidget7 = constraintWidget11;
                            constraintWidget8 = constraintWidget10;
                            i4 = i10;
                            constraintWidgetContainer = this;
                        }
                        constraintWidget5 = constraintWidget;
                        margin = i2;
                        linearSystem3 = linearSystem2;
                        i3 = 0;
                        if (i5 != 0) {
                            ConstraintAnchor constraintAnchor5 = constraintWidget2.mLeft;
                            ConstraintAnchor constraintAnchor6 = constraintWidget7.mRight;
                            margin2 = constraintAnchor5.getMargin();
                            i = constraintAnchor6.getMargin();
                            solverVariable = constraintWidget5.mLeft.mTarget != null ? constraintWidget5.mLeft.mTarget.mSolverVariable : null;
                            solverVariable2 = constraintWidget7.mRight.mTarget != null ? constraintWidget7.mRight.mTarget.mSolverVariable : null;
                            if (!(solverVariable == null || solverVariable2 == null)) {
                                linearSystem3.addLowerThan(constraintAnchor6.mSolverVariable, solverVariable2, -i, 1);
                                linearSystem3.addCentering(constraintAnchor5.mSolverVariable, solverVariable, margin2, constraintWidget5.mHorizontalBiasPercent, solverVariable2, constraintAnchor6.mSolverVariable, i, 4);
                            }
                        }
                        i2 = margin + 1;
                        linearSystem2 = linearSystem3;
                        i = i3;
                        constraintWidgetContainer = this;
                    }
                }
            }
            margin = i2;
            i3 = i;
            linearSystem3 = linearSystem2;
            i2 = margin + 1;
            linearSystem2 = linearSystem3;
            i = i3;
            constraintWidgetContainer = this;
        }
    }

    private void applyVerticalChain(LinearSystem linearSystem) {
        ConstraintWidgetContainer constraintWidgetContainer = this;
        LinearSystem linearSystem2 = linearSystem;
        int i = 0;
        int i2 = 0;
        while (i2 < constraintWidgetContainer.mVerticalChainsSize) {
            int margin;
            LinearSystem linearSystem3;
            int i3;
            ConstraintWidget constraintWidget = constraintWidgetContainer.mVerticalChainsArray[i2];
            int countMatchConstraintsChainedWidgets = constraintWidgetContainer.countMatchConstraintsChainedWidgets(linearSystem2, constraintWidgetContainer.mChainEnds, constraintWidgetContainer.mVerticalChainsArray[i2], 1, constraintWidgetContainer.flags);
            ConstraintWidget constraintWidget2 = constraintWidgetContainer.mChainEnds[2];
            if (constraintWidget2 != null) {
                if (constraintWidgetContainer.flags[1]) {
                    countMatchConstraintsChainedWidgets = constraintWidget.getDrawY();
                    while (constraintWidget2 != null) {
                        linearSystem2.addEquality(constraintWidget2.mTop.mSolverVariable, countMatchConstraintsChainedWidgets);
                        countMatchConstraintsChainedWidgets += (constraintWidget2.mTop.getMargin() + constraintWidget2.getHeight()) + constraintWidget2.mBottom.getMargin();
                        constraintWidget2 = constraintWidget2.mVerticalNextWidget;
                    }
                } else {
                    int i4 = constraintWidget.mVerticalChainStyle == 0 ? 1 : i;
                    int i5 = constraintWidget.mVerticalChainStyle == 2 ? 1 : i;
                    int i6 = constraintWidgetContainer.mVerticalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT ? 1 : i;
                    if ((constraintWidgetContainer.mOptimizationLevel == 2 || constraintWidgetContainer.mOptimizationLevel == 8) && constraintWidgetContainer.flags[i] && constraintWidget.mVerticalChainFixedPosition && i5 == 0 && i6 == 0 && constraintWidget.mVerticalChainStyle == 0) {
                        Optimizer.applyDirectResolutionVerticalChain(constraintWidgetContainer, linearSystem2, countMatchConstraintsChainedWidgets, constraintWidget);
                    } else {
                        ConstraintWidget constraintWidget3;
                        ConstraintWidget constraintWidget4;
                        SolverVariable solverVariable;
                        SolverVariable solverVariable2;
                        ConstraintWidget constraintWidget5;
                        int margin2;
                        SolverVariable solverVariable3;
                        int i7 = 3;
                        if (countMatchConstraintsChainedWidgets != 0) {
                            if (i5 == 0) {
                                float f = 0.0f;
                                constraintWidget3 = null;
                                while (constraintWidget2 != null) {
                                    if (constraintWidget2.mVerticalDimensionBehaviour != DimensionBehaviour.MATCH_CONSTRAINT) {
                                        i6 = constraintWidget2.mTop.getMargin();
                                        if (constraintWidget3 != null) {
                                            i6 += constraintWidget3.mBottom.getMargin();
                                        }
                                        linearSystem2.addGreaterThan(constraintWidget2.mTop.mSolverVariable, constraintWidget2.mTop.mTarget.mSolverVariable, i6, constraintWidget2.mTop.mTarget.mOwner.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT ? 2 : 3);
                                        i4 = constraintWidget2.mBottom.getMargin();
                                        if (constraintWidget2.mBottom.mTarget.mOwner.mTop.mTarget != null && constraintWidget2.mBottom.mTarget.mOwner.mTop.mTarget.mOwner == constraintWidget2) {
                                            i4 += constraintWidget2.mBottom.mTarget.mOwner.mTop.getMargin();
                                        }
                                        linearSystem2.addLowerThan(constraintWidget2.mBottom.mSolverVariable, constraintWidget2.mBottom.mTarget.mSolverVariable, -i4, constraintWidget2.mBottom.mTarget.mOwner.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT ? 2 : 3);
                                    } else {
                                        f += constraintWidget2.mVerticalWeight;
                                        if (constraintWidget2.mBottom.mTarget != null) {
                                            i4 = constraintWidget2.mBottom.getMargin();
                                            if (constraintWidget2 != constraintWidgetContainer.mChainEnds[3]) {
                                                i4 += constraintWidget2.mBottom.mTarget.mOwner.mTop.getMargin();
                                            }
                                        } else {
                                            i4 = i;
                                        }
                                        linearSystem2.addGreaterThan(constraintWidget2.mBottom.mSolverVariable, constraintWidget2.mTop.mSolverVariable, i, 1);
                                        linearSystem2.addLowerThan(constraintWidget2.mBottom.mSolverVariable, constraintWidget2.mBottom.mTarget.mSolverVariable, -i4, 1);
                                    }
                                    constraintWidget3 = constraintWidget2;
                                    constraintWidget2 = constraintWidget2.mVerticalNextWidget;
                                }
                                int i8;
                                if (countMatchConstraintsChainedWidgets != 1) {
                                    i8 = i;
                                    while (true) {
                                        i4 = countMatchConstraintsChainedWidgets - 1;
                                        if (i8 >= i4) {
                                            break;
                                        }
                                        ConstraintWidget constraintWidget6 = constraintWidgetContainer.mMatchConstraintsChainedWidgets[i8];
                                        i8++;
                                        constraintWidget4 = constraintWidgetContainer.mMatchConstraintsChainedWidgets[i8];
                                        solverVariable = constraintWidget6.mTop.mSolverVariable;
                                        solverVariable2 = constraintWidget6.mBottom.mSolverVariable;
                                        SolverVariable solverVariable4 = constraintWidget4.mTop.mSolverVariable;
                                        SolverVariable solverVariable5 = constraintWidget4.mBottom.mSolverVariable;
                                        if (constraintWidget4 == constraintWidgetContainer.mChainEnds[i7]) {
                                            solverVariable5 = constraintWidgetContainer.mChainEnds[1].mBottom.mSolverVariable;
                                        }
                                        margin = constraintWidget6.mTop.getMargin();
                                        if (!(constraintWidget6.mTop.mTarget == null || constraintWidget6.mTop.mTarget.mOwner.mBottom.mTarget == null || constraintWidget6.mTop.mTarget.mOwner.mBottom.mTarget.mOwner != constraintWidget6)) {
                                            margin += constraintWidget6.mTop.mTarget.mOwner.mBottom.getMargin();
                                        }
                                        int i9 = countMatchConstraintsChainedWidgets;
                                        linearSystem2.addGreaterThan(solverVariable, constraintWidget6.mTop.mTarget.mSolverVariable, margin, 2);
                                        countMatchConstraintsChainedWidgets = constraintWidget6.mBottom.getMargin();
                                        if (!(constraintWidget6.mBottom.mTarget == null || constraintWidget6.mVerticalNextWidget == null)) {
                                            countMatchConstraintsChainedWidgets += constraintWidget6.mVerticalNextWidget.mTop.mTarget != null ? constraintWidget6.mVerticalNextWidget.mTop.getMargin() : 0;
                                        }
                                        linearSystem2.addLowerThan(solverVariable2, constraintWidget6.mBottom.mTarget.mSolverVariable, -countMatchConstraintsChainedWidgets, 2);
                                        if (i8 == i4) {
                                            countMatchConstraintsChainedWidgets = constraintWidget4.mTop.getMargin();
                                            if (!(constraintWidget4.mTop.mTarget == null || constraintWidget4.mTop.mTarget.mOwner.mBottom.mTarget == null || constraintWidget4.mTop.mTarget.mOwner.mBottom.mTarget.mOwner != constraintWidget4)) {
                                                countMatchConstraintsChainedWidgets += constraintWidget4.mTop.mTarget.mOwner.mBottom.getMargin();
                                            }
                                            linearSystem2.addGreaterThan(solverVariable4, constraintWidget4.mTop.mTarget.mSolverVariable, countMatchConstraintsChainedWidgets, 2);
                                            ConstraintAnchor constraintAnchor = constraintWidget4.mBottom;
                                            if (constraintWidget4 == constraintWidgetContainer.mChainEnds[3]) {
                                                constraintAnchor = constraintWidgetContainer.mChainEnds[1].mBottom;
                                            }
                                            i4 = constraintAnchor.getMargin();
                                            if (!(constraintAnchor.mTarget == null || constraintAnchor.mTarget.mOwner.mTop.mTarget == null || constraintAnchor.mTarget.mOwner.mTop.mTarget.mOwner != constraintWidget4)) {
                                                i4 += constraintAnchor.mTarget.mOwner.mTop.getMargin();
                                            }
                                            margin = 2;
                                            linearSystem2.addLowerThan(solverVariable5, constraintAnchor.mTarget.mSolverVariable, -i4, 2);
                                        } else {
                                            margin = 2;
                                        }
                                        if (constraintWidget.mMatchConstraintMaxHeight > 0) {
                                            linearSystem2.addLowerThan(solverVariable2, solverVariable, constraintWidget.mMatchConstraintMaxHeight, margin);
                                        }
                                        ArrayRow createRow = linearSystem.createRow();
                                        createRow.createRowEqualDimension(constraintWidget6.mVerticalWeight, f, constraintWidget4.mVerticalWeight, solverVariable, constraintWidget6.mTop.getMargin(), solverVariable2, constraintWidget6.mBottom.getMargin(), solverVariable4, constraintWidget4.mTop.getMargin(), solverVariable5, constraintWidget4.mBottom.getMargin());
                                        linearSystem2.addConstraint(createRow);
                                        countMatchConstraintsChainedWidgets = i9;
                                        i7 = 3;
                                        i = 0;
                                    }
                                } else {
                                    constraintWidget5 = constraintWidgetContainer.mMatchConstraintsChainedWidgets[i];
                                    i8 = constraintWidget5.mTop.getMargin();
                                    if (constraintWidget5.mTop.mTarget != null) {
                                        i8 += constraintWidget5.mTop.mTarget.getMargin();
                                    }
                                    i4 = constraintWidget5.mBottom.getMargin();
                                    if (constraintWidget5.mBottom.mTarget != null) {
                                        i4 += constraintWidget5.mBottom.mTarget.getMargin();
                                    }
                                    SolverVariable solverVariable6 = constraintWidget.mBottom.mTarget.mSolverVariable;
                                    if (constraintWidget5 == constraintWidgetContainer.mChainEnds[3]) {
                                        solverVariable6 = constraintWidgetContainer.mChainEnds[1].mBottom.mTarget.mSolverVariable;
                                    }
                                    if (constraintWidget5.mMatchConstraintDefaultHeight == 1) {
                                        linearSystem2.addGreaterThan(constraintWidget.mTop.mSolverVariable, constraintWidget.mTop.mTarget.mSolverVariable, i8, 1);
                                        linearSystem2.addLowerThan(constraintWidget.mBottom.mSolverVariable, solverVariable6, -i4, 1);
                                        linearSystem2.addEquality(constraintWidget.mBottom.mSolverVariable, constraintWidget.mTop.mSolverVariable, constraintWidget.getHeight(), 2);
                                    } else {
                                        linearSystem2.addEquality(constraintWidget5.mTop.mSolverVariable, constraintWidget5.mTop.mTarget.mSolverVariable, i8, 1);
                                        linearSystem2.addEquality(constraintWidget5.mBottom.mSolverVariable, solverVariable6, -i4, 1);
                                    }
                                }
                            }
                        }
                        constraintWidget5 = constraintWidget2;
                        ConstraintWidget constraintWidget7 = null;
                        ConstraintWidget constraintWidget8 = constraintWidget7;
                        Object obj = null;
                        while (constraintWidget5 != null) {
                            ConstraintWidget constraintWidget9;
                            Object obj2;
                            ConstraintWidget constraintWidget10;
                            int i10;
                            ConstraintWidget constraintWidget11;
                            constraintWidget4 = constraintWidget5.mVerticalNextWidget;
                            if (constraintWidget4 == null) {
                                constraintWidget9 = constraintWidgetContainer.mChainEnds[1];
                                obj2 = 1;
                            } else {
                                constraintWidget9 = constraintWidget7;
                                obj2 = obj;
                            }
                            ConstraintAnchor constraintAnchor2;
                            int margin3;
                            if (i5 != 0) {
                                SolverVariable solverVariable7;
                                constraintAnchor2 = constraintWidget5.mTop;
                                margin3 = constraintAnchor2.getMargin();
                                if (constraintWidget8 != null) {
                                    margin3 += constraintWidget8.mBottom.getMargin();
                                }
                                margin = constraintWidget2 != constraintWidget5 ? 3 : 1;
                                if (constraintAnchor2.mTarget != null) {
                                    solverVariable2 = constraintAnchor2.mSolverVariable;
                                    solverVariable7 = constraintAnchor2.mTarget.mSolverVariable;
                                } else if (constraintWidget5.mBaseline.mTarget != null) {
                                    solverVariable2 = constraintWidget5.mBaseline.mSolverVariable;
                                    solverVariable7 = constraintWidget5.mBaseline.mTarget.mSolverVariable;
                                    margin3 -= constraintAnchor2.getMargin();
                                } else {
                                    solverVariable2 = null;
                                    solverVariable7 = solverVariable2;
                                }
                                if (!(solverVariable2 == null || solverVariable7 == null)) {
                                    linearSystem2.addGreaterThan(solverVariable2, solverVariable7, margin3, margin);
                                }
                                if (constraintWidget5.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                                    ConstraintAnchor constraintAnchor3 = constraintWidget5.mBottom;
                                    if (constraintWidget5.mMatchConstraintDefaultHeight == 1) {
                                        linearSystem2.addEquality(constraintAnchor3.mSolverVariable, constraintAnchor2.mSolverVariable, Math.max(constraintWidget5.mMatchConstraintMinHeight, constraintWidget5.getHeight()), 3);
                                    } else {
                                        linearSystem2.addGreaterThan(constraintAnchor2.mSolverVariable, constraintAnchor2.mTarget.mSolverVariable, constraintAnchor2.mMargin, 3);
                                        linearSystem2.addLowerThan(constraintAnchor3.mSolverVariable, constraintAnchor2.mSolverVariable, constraintWidget5.mMatchConstraintMinHeight, 3);
                                    }
                                }
                            } else if (i4 != 0 || obj2 == null || constraintWidget8 == null) {
                                if (i4 != 0 || obj2 != null || constraintWidget8 != null) {
                                    ConstraintWidget constraintWidget12;
                                    ConstraintAnchor constraintAnchor4 = constraintWidget5.mTop;
                                    constraintAnchor2 = constraintWidget5.mBottom;
                                    margin2 = constraintAnchor4.getMargin();
                                    margin3 = constraintAnchor2.getMargin();
                                    constraintWidget10 = constraintWidget5;
                                    i10 = i4;
                                    linearSystem2.addGreaterThan(constraintAnchor4.mSolverVariable, constraintAnchor4.mTarget.mSolverVariable, margin2, 1);
                                    int i11 = i2;
                                    linearSystem2.addLowerThan(constraintAnchor2.mSolverVariable, constraintAnchor2.mTarget.mSolverVariable, -margin3, 1);
                                    SolverVariable solverVariable8 = constraintAnchor4.mTarget != null ? constraintAnchor4.mTarget.mSolverVariable : null;
                                    if (constraintWidget8 == null) {
                                        solverVariable8 = constraintWidget.mTop.mTarget != null ? constraintWidget.mTop.mTarget.mSolverVariable : null;
                                    }
                                    if (constraintWidget4 == null) {
                                        constraintWidget4 = constraintWidget9.mBottom.mTarget != null ? constraintWidget9.mBottom.mTarget.mOwner : null;
                                    }
                                    constraintWidget3 = constraintWidget4;
                                    if (constraintWidget3 != null) {
                                        SolverVariable solverVariable9 = constraintWidget3.mTop.mSolverVariable;
                                        if (obj2 != null) {
                                            solverVariable9 = constraintWidget9.mBottom.mTarget != null ? constraintWidget9.mBottom.mTarget.mSolverVariable : null;
                                        }
                                        if (!(solverVariable8 == null || solverVariable9 == null)) {
                                            int i12 = margin3;
                                            solverVariable = solverVariable8;
                                            constraintWidget5 = constraintWidget;
                                            solverVariable3 = solverVariable9;
                                            margin = i11;
                                            constraintWidget11 = constraintWidget9;
                                            constraintWidget12 = constraintWidget3;
                                            linearSystem3 = linearSystem2;
                                            linearSystem2.addCentering(constraintAnchor4.mSolverVariable, solverVariable, margin2, 0.5f, solverVariable3, constraintAnchor2.mSolverVariable, i12, 4);
                                            constraintWidget4 = constraintWidget12;
                                            if (obj2 == null) {
                                                constraintWidget4 = null;
                                            }
                                            constraintWidget = constraintWidget5;
                                            obj = obj2;
                                            linearSystem2 = linearSystem3;
                                            i2 = margin;
                                            constraintWidget5 = constraintWidget4;
                                            constraintWidget7 = constraintWidget11;
                                            constraintWidget8 = constraintWidget10;
                                            i4 = i10;
                                            constraintWidgetContainer = this;
                                        }
                                    }
                                    constraintWidget12 = constraintWidget3;
                                    constraintWidget5 = constraintWidget;
                                    constraintWidget11 = constraintWidget9;
                                    linearSystem3 = linearSystem2;
                                    margin = i11;
                                    constraintWidget4 = constraintWidget12;
                                    if (obj2 == null) {
                                        constraintWidget4 = null;
                                    }
                                    constraintWidget = constraintWidget5;
                                    obj = obj2;
                                    linearSystem2 = linearSystem3;
                                    i2 = margin;
                                    constraintWidget5 = constraintWidget4;
                                    constraintWidget7 = constraintWidget11;
                                    constraintWidget8 = constraintWidget10;
                                    i4 = i10;
                                    constraintWidgetContainer = this;
                                } else if (constraintWidget5.mTop.mTarget == null) {
                                    linearSystem2.addEquality(constraintWidget5.mTop.mSolverVariable, constraintWidget5.getDrawY());
                                } else {
                                    linearSystem2.addEquality(constraintWidget5.mTop.mSolverVariable, constraintWidget.mTop.mTarget.mSolverVariable, constraintWidget5.mTop.getMargin(), 5);
                                }
                            } else if (constraintWidget5.mBottom.mTarget == null) {
                                linearSystem2.addEquality(constraintWidget5.mBottom.mSolverVariable, constraintWidget5.getDrawBottom());
                            } else {
                                linearSystem2.addEquality(constraintWidget5.mBottom.mSolverVariable, constraintWidget9.mBottom.mTarget.mSolverVariable, -constraintWidget5.mBottom.getMargin(), 5);
                            }
                            constraintWidget10 = constraintWidget5;
                            i10 = i4;
                            constraintWidget5 = constraintWidget;
                            margin = i2;
                            constraintWidget11 = constraintWidget9;
                            linearSystem3 = linearSystem2;
                            if (obj2 == null) {
                                constraintWidget4 = null;
                            }
                            constraintWidget = constraintWidget5;
                            obj = obj2;
                            linearSystem2 = linearSystem3;
                            i2 = margin;
                            constraintWidget5 = constraintWidget4;
                            constraintWidget7 = constraintWidget11;
                            constraintWidget8 = constraintWidget10;
                            i4 = i10;
                            constraintWidgetContainer = this;
                        }
                        constraintWidget5 = constraintWidget;
                        margin = i2;
                        linearSystem3 = linearSystem2;
                        i3 = 0;
                        if (i5 != 0) {
                            ConstraintAnchor constraintAnchor5 = constraintWidget2.mTop;
                            ConstraintAnchor constraintAnchor6 = constraintWidget7.mBottom;
                            margin2 = constraintAnchor5.getMargin();
                            i = constraintAnchor6.getMargin();
                            solverVariable = constraintWidget5.mTop.mTarget != null ? constraintWidget5.mTop.mTarget.mSolverVariable : null;
                            solverVariable3 = constraintWidget7.mBottom.mTarget != null ? constraintWidget7.mBottom.mTarget.mSolverVariable : null;
                            if (!(solverVariable == null || solverVariable3 == null)) {
                                linearSystem3.addLowerThan(constraintAnchor6.mSolverVariable, solverVariable3, -i, 1);
                                linearSystem3.addCentering(constraintAnchor5.mSolverVariable, solverVariable, margin2, constraintWidget5.mVerticalBiasPercent, solverVariable3, constraintAnchor6.mSolverVariable, i, 4);
                            }
                        }
                        i2 = margin + 1;
                        linearSystem2 = linearSystem3;
                        i = i3;
                        constraintWidgetContainer = this;
                    }
                }
            }
            margin = i2;
            i3 = i;
            linearSystem3 = linearSystem2;
            i2 = margin + 1;
            linearSystem2 = linearSystem3;
            i = i3;
            constraintWidgetContainer = this;
        }
    }

    public void updateChildrenFromSolver(LinearSystem linearSystem, int i, boolean[] zArr) {
        int i2 = 0;
        zArr[2] = false;
        updateFromSolver(linearSystem, i);
        int size = this.mChildren.size();
        while (i2 < size) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i2);
            constraintWidget.updateFromSolver(linearSystem, i);
            if (constraintWidget.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && constraintWidget.getWidth() < constraintWidget.getWrapWidth()) {
                zArr[2] = true;
            }
            if (constraintWidget.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && constraintWidget.getHeight() < constraintWidget.getWrapHeight()) {
                zArr[2] = true;
            }
            i2++;
        }
    }

    public void setPadding(int i, int i2, int i3, int i4) {
        this.mPaddingLeft = i;
        this.mPaddingTop = i2;
        this.mPaddingRight = i3;
        this.mPaddingBottom = i4;
    }

    public void layout() {
        boolean z;
        int i;
        boolean addChildrenToSolver;
        Exception e;
        int i2;
        int i3 = this.mX;
        int i4 = this.mY;
        int max = Math.max(0, getWidth());
        int max2 = Math.max(0, getHeight());
        this.mWidthMeasuredTooSmall = false;
        this.mHeightMeasuredTooSmall = false;
        if (this.mParent != null) {
            if (r1.mSnapshot == null) {
                r1.mSnapshot = new Snapshot(r1);
            }
            r1.mSnapshot.updateFrom(r1);
            setX(r1.mPaddingLeft);
            setY(r1.mPaddingTop);
            resetAnchors();
            resetSolverVariables(r1.mSystem.getCache());
        } else {
            r1.mX = 0;
            r1.mY = 0;
        }
        DimensionBehaviour dimensionBehaviour = r1.mVerticalDimensionBehaviour;
        DimensionBehaviour dimensionBehaviour2 = r1.mHorizontalDimensionBehaviour;
        int i5 = 2;
        boolean z2 = true;
        if (r1.mOptimizationLevel == 2 && (r1.mVerticalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT || r1.mHorizontalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT)) {
            findWrapSize(r1.mChildren, r1.flags);
            z = r1.flags[0];
            if (max > 0 && max2 > 0 && (r1.mWrapWidth > max || r1.mWrapHeight > max2)) {
                z = false;
            }
            if (z) {
                if (r1.mHorizontalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                    r1.mHorizontalDimensionBehaviour = DimensionBehaviour.FIXED;
                    if (max <= 0 || max >= r1.mWrapWidth) {
                        setWidth(Math.max(r1.mMinWidth, r1.mWrapWidth));
                    } else {
                        r1.mWidthMeasuredTooSmall = true;
                        setWidth(max);
                    }
                }
                if (r1.mVerticalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                    r1.mVerticalDimensionBehaviour = DimensionBehaviour.FIXED;
                    if (max2 <= 0 || max2 >= r1.mWrapHeight) {
                        setHeight(Math.max(r1.mMinHeight, r1.mWrapHeight));
                    } else {
                        r1.mHeightMeasuredTooSmall = true;
                        setHeight(max2);
                    }
                }
            }
        } else {
            z = false;
        }
        resetChains();
        int size = r1.mChildren.size();
        for (i = 0; i < size; i++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) r1.mChildren.get(i);
            if (constraintWidget instanceof WidgetContainer) {
                ((WidgetContainer) constraintWidget).layout();
            }
        }
        i = 0;
        boolean z3 = z;
        z = true;
        while (z) {
            int i6;
            ConstraintWidget constraintWidget2;
            i += z2;
            try {
                r1.mSystem.reset();
                addChildrenToSolver = addChildrenToSolver(r1.mSystem, Integer.MAX_VALUE);
                if (addChildrenToSolver) {
                    try {
                        r1.mSystem.minimize();
                    } catch (Exception e2) {
                        e = e2;
                        z = addChildrenToSolver;
                        e.printStackTrace();
                        addChildrenToSolver = z;
                        if (!addChildrenToSolver) {
                            updateChildrenFromSolver(r1.mSystem, Integer.MAX_VALUE, r1.flags);
                        } else {
                            updateFromSolver(r1.mSystem, Integer.MAX_VALUE);
                            i6 = 0;
                            while (i6 < size) {
                                constraintWidget2 = (ConstraintWidget) r1.mChildren.get(i6);
                                if (constraintWidget2.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                                }
                                if (constraintWidget2.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                                }
                                i6++;
                                i5 = 2;
                            }
                        }
                        i2 = i5;
                        if (i < 8) {
                        }
                        z2 = z3;
                        addChildrenToSolver = false;
                        i2 = Math.max(r1.mMinWidth, getWidth());
                        if (i2 > getWidth()) {
                            setWidth(i2);
                            r1.mHorizontalDimensionBehaviour = DimensionBehaviour.FIXED;
                            addChildrenToSolver = true;
                            z2 = true;
                        }
                        i2 = Math.max(r1.mMinHeight, getHeight());
                        if (i2 > getHeight()) {
                            setHeight(i2);
                            r1.mVerticalDimensionBehaviour = DimensionBehaviour.FIXED;
                            addChildrenToSolver = true;
                            z2 = true;
                        }
                        if (!z2) {
                            r1.mWidthMeasuredTooSmall = true;
                            r1.mHorizontalDimensionBehaviour = DimensionBehaviour.FIXED;
                            setWidth(max);
                            addChildrenToSolver = true;
                            z2 = true;
                            z = true;
                            r1.mHeightMeasuredTooSmall = true;
                            r1.mVerticalDimensionBehaviour = DimensionBehaviour.FIXED;
                            setHeight(max2);
                            addChildrenToSolver = true;
                            z3 = addChildrenToSolver;
                            z2 = z;
                            i5 = 2;
                            z = addChildrenToSolver;
                        }
                        z = true;
                        z3 = z2;
                        z2 = z;
                        i5 = 2;
                        z = addChildrenToSolver;
                    }
                }
            } catch (Exception e3) {
                e = e3;
                e.printStackTrace();
                addChildrenToSolver = z;
                if (!addChildrenToSolver) {
                    updateFromSolver(r1.mSystem, Integer.MAX_VALUE);
                    i6 = 0;
                    while (i6 < size) {
                        constraintWidget2 = (ConstraintWidget) r1.mChildren.get(i6);
                        if (constraintWidget2.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                        }
                        if (constraintWidget2.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                        }
                        i6++;
                        i5 = 2;
                    }
                } else {
                    updateChildrenFromSolver(r1.mSystem, Integer.MAX_VALUE, r1.flags);
                }
                i2 = i5;
                if (i < 8) {
                }
                z2 = z3;
                addChildrenToSolver = false;
                i2 = Math.max(r1.mMinWidth, getWidth());
                if (i2 > getWidth()) {
                    setWidth(i2);
                    r1.mHorizontalDimensionBehaviour = DimensionBehaviour.FIXED;
                    addChildrenToSolver = true;
                    z2 = true;
                }
                i2 = Math.max(r1.mMinHeight, getHeight());
                if (i2 > getHeight()) {
                    setHeight(i2);
                    r1.mVerticalDimensionBehaviour = DimensionBehaviour.FIXED;
                    addChildrenToSolver = true;
                    z2 = true;
                }
                if (z2) {
                    r1.mWidthMeasuredTooSmall = true;
                    r1.mHorizontalDimensionBehaviour = DimensionBehaviour.FIXED;
                    setWidth(max);
                    addChildrenToSolver = true;
                    z2 = true;
                    z = true;
                    r1.mHeightMeasuredTooSmall = true;
                    r1.mVerticalDimensionBehaviour = DimensionBehaviour.FIXED;
                    setHeight(max2);
                    addChildrenToSolver = true;
                    z3 = addChildrenToSolver;
                    z2 = z;
                    i5 = 2;
                    z = addChildrenToSolver;
                }
                z = true;
                z3 = z2;
                z2 = z;
                i5 = 2;
                z = addChildrenToSolver;
            }
            if (!addChildrenToSolver) {
                updateFromSolver(r1.mSystem, Integer.MAX_VALUE);
                i6 = 0;
                while (i6 < size) {
                    constraintWidget2 = (ConstraintWidget) r1.mChildren.get(i6);
                    if (constraintWidget2.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT || constraintWidget2.getWidth() >= constraintWidget2.getWrapWidth()) {
                        if (constraintWidget2.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && constraintWidget2.getHeight() < constraintWidget2.getWrapHeight()) {
                            i2 = 2;
                            r1.flags[2] = z2;
                            break;
                        }
                        i6++;
                        i5 = 2;
                    } else {
                        i5 = 2;
                        r1.flags[2] = z2;
                        break;
                    }
                }
            }
            updateChildrenFromSolver(r1.mSystem, Integer.MAX_VALUE, r1.flags);
            i2 = i5;
            if (i < 8 || !r1.flags[r9]) {
                z2 = z3;
                addChildrenToSolver = false;
            } else {
                i5 = 0;
                int i7 = 0;
                for (i6 = 0; i6 < size; i6++) {
                    constraintWidget2 = (ConstraintWidget) r1.mChildren.get(i6);
                    i5 = Math.max(i5, constraintWidget2.mX + constraintWidget2.getWidth());
                    i7 = Math.max(i7, constraintWidget2.mY + constraintWidget2.getHeight());
                }
                i6 = Math.max(r1.mMinWidth, i5);
                i2 = Math.max(r1.mMinHeight, i7);
                if (dimensionBehaviour2 != DimensionBehaviour.WRAP_CONTENT || getWidth() >= i6) {
                    z2 = z3;
                    addChildrenToSolver = false;
                } else {
                    setWidth(i6);
                    r1.mHorizontalDimensionBehaviour = DimensionBehaviour.WRAP_CONTENT;
                    addChildrenToSolver = true;
                    z2 = true;
                }
                if (dimensionBehaviour == DimensionBehaviour.WRAP_CONTENT && getHeight() < i2) {
                    setHeight(i2);
                    r1.mVerticalDimensionBehaviour = DimensionBehaviour.WRAP_CONTENT;
                    addChildrenToSolver = true;
                    z2 = true;
                }
            }
            i2 = Math.max(r1.mMinWidth, getWidth());
            if (i2 > getWidth()) {
                setWidth(i2);
                r1.mHorizontalDimensionBehaviour = DimensionBehaviour.FIXED;
                addChildrenToSolver = true;
                z2 = true;
            }
            i2 = Math.max(r1.mMinHeight, getHeight());
            if (i2 > getHeight()) {
                setHeight(i2);
                r1.mVerticalDimensionBehaviour = DimensionBehaviour.FIXED;
                addChildrenToSolver = true;
                z2 = true;
            }
            if (z2) {
                if (r1.mHorizontalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT && max > 0 && getWidth() > max) {
                    r1.mWidthMeasuredTooSmall = true;
                    r1.mHorizontalDimensionBehaviour = DimensionBehaviour.FIXED;
                    setWidth(max);
                    addChildrenToSolver = true;
                    z2 = true;
                }
                if (r1.mVerticalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT && max2 > 0 && getHeight() > max2) {
                    z = true;
                    r1.mHeightMeasuredTooSmall = true;
                    r1.mVerticalDimensionBehaviour = DimensionBehaviour.FIXED;
                    setHeight(max2);
                    addChildrenToSolver = true;
                    z3 = addChildrenToSolver;
                    z2 = z;
                    i5 = 2;
                    z = addChildrenToSolver;
                }
            }
            z = true;
            z3 = z2;
            z2 = z;
            i5 = 2;
            z = addChildrenToSolver;
        }
        if (r1.mParent != null) {
            i3 = Math.max(r1.mMinWidth, getWidth());
            i4 = Math.max(r1.mMinHeight, getHeight());
            r1.mSnapshot.applyTo(r1);
            setWidth((i3 + r1.mPaddingLeft) + r1.mPaddingRight);
            setHeight((i4 + r1.mPaddingTop) + r1.mPaddingBottom);
        } else {
            r1.mX = i3;
            r1.mY = i4;
        }
        if (z3) {
            r1.mHorizontalDimensionBehaviour = dimensionBehaviour2;
            r1.mVerticalDimensionBehaviour = dimensionBehaviour;
        }
        resetSolverVariables(r1.mSystem.getCache());
        if (r1 == getRootConstraintContainer()) {
            updateDrawPosition();
        }
    }

    static int setGroup(ConstraintAnchor constraintAnchor, int i) {
        int i2 = constraintAnchor.mGroup;
        if (constraintAnchor.mOwner.getParent() == null) {
            return i;
        }
        if (i2 <= i) {
            return i2;
        }
        constraintAnchor.mGroup = i;
        ConstraintAnchor opposite = constraintAnchor.getOpposite();
        ConstraintAnchor constraintAnchor2 = constraintAnchor.mTarget;
        if (opposite != null) {
            i = setGroup(opposite, i);
        }
        if (constraintAnchor2 != null) {
            i = setGroup(constraintAnchor2, i);
        }
        if (opposite != null) {
            i = setGroup(opposite, i);
        }
        constraintAnchor.mGroup = i;
        return i;
    }

    public int layoutFindGroupsSimple() {
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i);
            constraintWidget.mLeft.mGroup = 0;
            constraintWidget.mRight.mGroup = 0;
            constraintWidget.mTop.mGroup = 1;
            constraintWidget.mBottom.mGroup = 1;
            constraintWidget.mBaseline.mGroup = 1;
        }
        return 2;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void findHorizontalWrapRecursive(android.support.constraint.solver.widgets.ConstraintWidget r8, boolean[] r9) {
        /*
        r7 = this;
        r0 = r8.mHorizontalDimensionBehaviour;
        r1 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        r2 = 0;
        r3 = 0;
        if (r0 != r1) goto L_0x0017;
    L_0x0008:
        r0 = r8.mVerticalDimensionBehaviour;
        r1 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        if (r0 != r1) goto L_0x0017;
    L_0x000e:
        r0 = r8.mDimensionRatio;
        r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r0 <= 0) goto L_0x0017;
    L_0x0014:
        r9[r3] = r3;
        return;
    L_0x0017:
        r0 = r8.getOptimizerWrapWidth();
        r1 = r8.mHorizontalDimensionBehaviour;
        r4 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        if (r1 != r4) goto L_0x0030;
    L_0x0021:
        r1 = r8.mVerticalDimensionBehaviour;
        r4 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        if (r1 == r4) goto L_0x0030;
    L_0x0027:
        r1 = r8.mDimensionRatio;
        r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r1 <= 0) goto L_0x0030;
    L_0x002d:
        r9[r3] = r3;
        return;
    L_0x0030:
        r1 = 1;
        r8.mHorizontalWrapVisited = r1;
        r2 = r8 instanceof android.support.constraint.solver.widgets.Guideline;
        if (r2 == 0) goto L_0x0060;
    L_0x0037:
        r9 = r8;
        r9 = (android.support.constraint.solver.widgets.Guideline) r9;
        r2 = r9.getOrientation();
        if (r2 != r1) goto L_0x005c;
    L_0x0040:
        r0 = r9.getRelativeBegin();
        r1 = -1;
        if (r0 == r1) goto L_0x004e;
    L_0x0047:
        r9 = r9.getRelativeBegin();
        r0 = r3;
        r3 = r9;
        goto L_0x005d;
    L_0x004e:
        r0 = r9.getRelativeEnd();
        if (r0 == r1) goto L_0x005a;
    L_0x0054:
        r9 = r9.getRelativeEnd();
        r0 = r9;
        goto L_0x005d;
    L_0x005a:
        r0 = r3;
        goto L_0x005d;
    L_0x005c:
        r3 = r0;
    L_0x005d:
        r5 = r0;
        goto L_0x01b3;
    L_0x0060:
        r2 = r8.mRight;
        r2 = r2.isConnected();
        if (r2 != 0) goto L_0x0077;
    L_0x0068:
        r2 = r8.mLeft;
        r2 = r2.isConnected();
        if (r2 != 0) goto L_0x0077;
    L_0x0070:
        r9 = r8.getX();
        r3 = r0 + r9;
        goto L_0x005d;
    L_0x0077:
        r2 = r8.mRight;
        r2 = r2.mTarget;
        if (r2 == 0) goto L_0x00a8;
    L_0x007d:
        r2 = r8.mLeft;
        r2 = r2.mTarget;
        if (r2 == 0) goto L_0x00a8;
    L_0x0083:
        r2 = r8.mRight;
        r2 = r2.mTarget;
        r4 = r8.mLeft;
        r4 = r4.mTarget;
        if (r2 == r4) goto L_0x00a5;
    L_0x008d:
        r2 = r8.mRight;
        r2 = r2.mTarget;
        r2 = r2.mOwner;
        r4 = r8.mLeft;
        r4 = r4.mTarget;
        r4 = r4.mOwner;
        if (r2 != r4) goto L_0x00a8;
    L_0x009b:
        r2 = r8.mRight;
        r2 = r2.mTarget;
        r2 = r2.mOwner;
        r4 = r8.mParent;
        if (r2 == r4) goto L_0x00a8;
    L_0x00a5:
        r9[r3] = r3;
        return;
    L_0x00a8:
        r2 = r8.mRight;
        r2 = r2.mTarget;
        r4 = 0;
        if (r2 == 0) goto L_0x00ca;
    L_0x00af:
        r2 = r8.mRight;
        r2 = r2.mTarget;
        r2 = r2.mOwner;
        r5 = r8.mRight;
        r5 = r5.getMargin();
        r5 = r5 + r0;
        r6 = r2.isRoot();
        if (r6 != 0) goto L_0x00cc;
    L_0x00c2:
        r6 = r2.mHorizontalWrapVisited;
        if (r6 != 0) goto L_0x00cc;
    L_0x00c6:
        r7.findHorizontalWrapRecursive(r2, r9);
        goto L_0x00cc;
    L_0x00ca:
        r5 = r0;
        r2 = r4;
    L_0x00cc:
        r6 = r8.mLeft;
        r6 = r6.mTarget;
        if (r6 == 0) goto L_0x00ec;
    L_0x00d2:
        r4 = r8.mLeft;
        r4 = r4.mTarget;
        r4 = r4.mOwner;
        r6 = r8.mLeft;
        r6 = r6.getMargin();
        r0 = r0 + r6;
        r6 = r4.isRoot();
        if (r6 != 0) goto L_0x00ec;
    L_0x00e5:
        r6 = r4.mHorizontalWrapVisited;
        if (r6 != 0) goto L_0x00ec;
    L_0x00e9:
        r7.findHorizontalWrapRecursive(r4, r9);
    L_0x00ec:
        r9 = r8.mRight;
        r9 = r9.mTarget;
        if (r9 == 0) goto L_0x014e;
    L_0x00f2:
        r9 = r2.isRoot();
        if (r9 != 0) goto L_0x014e;
    L_0x00f8:
        r9 = r8.mRight;
        r9 = r9.mTarget;
        r9 = r9.mType;
        r6 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.RIGHT;
        if (r9 != r6) goto L_0x010b;
    L_0x0102:
        r9 = r2.mDistToRight;
        r6 = r2.getOptimizerWrapWidth();
        r9 = r9 - r6;
        r5 = r5 + r9;
        goto L_0x011a;
    L_0x010b:
        r9 = r8.mRight;
        r9 = r9.mTarget;
        r9 = r9.getType();
        r6 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.LEFT;
        if (r9 != r6) goto L_0x011a;
    L_0x0117:
        r9 = r2.mDistToRight;
        r5 = r5 + r9;
    L_0x011a:
        r9 = r2.mRightHasCentered;
        if (r9 != 0) goto L_0x0133;
    L_0x011e:
        r9 = r2.mLeft;
        r9 = r9.mTarget;
        if (r9 == 0) goto L_0x0131;
    L_0x0124:
        r9 = r2.mRight;
        r9 = r9.mTarget;
        if (r9 == 0) goto L_0x0131;
    L_0x012a:
        r9 = r2.mHorizontalDimensionBehaviour;
        r6 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        if (r9 == r6) goto L_0x0131;
    L_0x0130:
        goto L_0x0133;
    L_0x0131:
        r9 = r3;
        goto L_0x0134;
    L_0x0133:
        r9 = r1;
    L_0x0134:
        r8.mRightHasCentered = r9;
        r9 = r8.mRightHasCentered;
        if (r9 == 0) goto L_0x014e;
    L_0x013a:
        r9 = r2.mLeft;
        r9 = r9.mTarget;
        if (r9 != 0) goto L_0x0141;
    L_0x0140:
        goto L_0x0149;
    L_0x0141:
        r9 = r2.mLeft;
        r9 = r9.mTarget;
        r9 = r9.mOwner;
        if (r9 == r8) goto L_0x014e;
    L_0x0149:
        r9 = r2.mDistToRight;
        r9 = r5 - r9;
        r5 = r5 + r9;
    L_0x014e:
        r9 = r8.mLeft;
        r9 = r9.mTarget;
        if (r9 == 0) goto L_0x01b2;
    L_0x0154:
        r9 = r4.isRoot();
        if (r9 != 0) goto L_0x01b2;
    L_0x015a:
        r9 = r8.mLeft;
        r9 = r9.mTarget;
        r9 = r9.getType();
        r2 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.LEFT;
        if (r9 != r2) goto L_0x016f;
    L_0x0166:
        r9 = r4.mDistToLeft;
        r2 = r4.getOptimizerWrapWidth();
        r9 = r9 - r2;
        r0 = r0 + r9;
        goto L_0x017e;
    L_0x016f:
        r9 = r8.mLeft;
        r9 = r9.mTarget;
        r9 = r9.getType();
        r2 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.RIGHT;
        if (r9 != r2) goto L_0x017e;
    L_0x017b:
        r9 = r4.mDistToLeft;
        r0 = r0 + r9;
    L_0x017e:
        r9 = r4.mLeftHasCentered;
        if (r9 != 0) goto L_0x0196;
    L_0x0182:
        r9 = r4.mLeft;
        r9 = r9.mTarget;
        if (r9 == 0) goto L_0x0195;
    L_0x0188:
        r9 = r4.mRight;
        r9 = r9.mTarget;
        if (r9 == 0) goto L_0x0195;
    L_0x018e:
        r9 = r4.mHorizontalDimensionBehaviour;
        r2 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        if (r9 == r2) goto L_0x0195;
    L_0x0194:
        goto L_0x0196;
    L_0x0195:
        r1 = r3;
    L_0x0196:
        r8.mLeftHasCentered = r1;
        r9 = r8.mLeftHasCentered;
        if (r9 == 0) goto L_0x01b2;
    L_0x019c:
        r9 = r4.mRight;
        r9 = r9.mTarget;
        if (r9 != 0) goto L_0x01a3;
    L_0x01a2:
        goto L_0x01ab;
    L_0x01a3:
        r9 = r4.mRight;
        r9 = r9.mTarget;
        r9 = r9.mOwner;
        if (r9 == r8) goto L_0x01b2;
    L_0x01ab:
        r9 = r4.mDistToLeft;
        r9 = r0 - r9;
        r3 = r0 + r9;
        goto L_0x01b3;
    L_0x01b2:
        r3 = r0;
    L_0x01b3:
        r9 = r8.getVisibility();
        r0 = 8;
        if (r9 != r0) goto L_0x01c1;
    L_0x01bb:
        r9 = r8.mWidth;
        r3 = r3 - r9;
        r9 = r8.mWidth;
        r5 = r5 - r9;
    L_0x01c1:
        r8.mDistToLeft = r3;
        r8.mDistToRight = r5;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.constraint.solver.widgets.ConstraintWidgetContainer.findHorizontalWrapRecursive(android.support.constraint.solver.widgets.ConstraintWidget, boolean[]):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void findVerticalWrapRecursive(android.support.constraint.solver.widgets.ConstraintWidget r9, boolean[] r10) {
        /*
        r8 = this;
        r0 = r9.mVerticalDimensionBehaviour;
        r1 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        r2 = 0;
        if (r0 != r1) goto L_0x0017;
    L_0x0007:
        r0 = r9.mHorizontalDimensionBehaviour;
        r1 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        if (r0 == r1) goto L_0x0017;
    L_0x000d:
        r0 = r9.mDimensionRatio;
        r1 = 0;
        r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1));
        if (r0 <= 0) goto L_0x0017;
    L_0x0014:
        r10[r2] = r2;
        return;
    L_0x0017:
        r0 = r9.getOptimizerWrapHeight();
        r1 = 1;
        r9.mVerticalWrapVisited = r1;
        r3 = r9 instanceof android.support.constraint.solver.widgets.Guideline;
        r4 = 8;
        if (r3 == 0) goto L_0x004d;
    L_0x0024:
        r10 = r9;
        r10 = (android.support.constraint.solver.widgets.Guideline) r10;
        r1 = r10.getOrientation();
        if (r1 != 0) goto L_0x0049;
    L_0x002d:
        r0 = r10.getRelativeBegin();
        r1 = -1;
        if (r0 == r1) goto L_0x003b;
    L_0x0034:
        r10 = r10.getRelativeBegin();
        r0 = r2;
        r2 = r10;
        goto L_0x004a;
    L_0x003b:
        r0 = r10.getRelativeEnd();
        if (r0 == r1) goto L_0x0047;
    L_0x0041:
        r10 = r10.getRelativeEnd();
        r0 = r10;
        goto L_0x004a;
    L_0x0047:
        r0 = r2;
        goto L_0x004a;
    L_0x0049:
        r2 = r0;
    L_0x004a:
        r6 = r2;
        goto L_0x0205;
    L_0x004d:
        r3 = r9.mBaseline;
        r3 = r3.mTarget;
        if (r3 != 0) goto L_0x0066;
    L_0x0053:
        r3 = r9.mTop;
        r3 = r3.mTarget;
        if (r3 != 0) goto L_0x0066;
    L_0x0059:
        r3 = r9.mBottom;
        r3 = r3.mTarget;
        if (r3 != 0) goto L_0x0066;
    L_0x005f:
        r10 = r9.getY();
        r2 = r0 + r10;
        goto L_0x004a;
    L_0x0066:
        r3 = r9.mBottom;
        r3 = r3.mTarget;
        if (r3 == 0) goto L_0x0097;
    L_0x006c:
        r3 = r9.mTop;
        r3 = r3.mTarget;
        if (r3 == 0) goto L_0x0097;
    L_0x0072:
        r3 = r9.mBottom;
        r3 = r3.mTarget;
        r5 = r9.mTop;
        r5 = r5.mTarget;
        if (r3 == r5) goto L_0x0094;
    L_0x007c:
        r3 = r9.mBottom;
        r3 = r3.mTarget;
        r3 = r3.mOwner;
        r5 = r9.mTop;
        r5 = r5.mTarget;
        r5 = r5.mOwner;
        if (r3 != r5) goto L_0x0097;
    L_0x008a:
        r3 = r9.mBottom;
        r3 = r3.mTarget;
        r3 = r3.mOwner;
        r5 = r9.mParent;
        if (r3 == r5) goto L_0x0097;
    L_0x0094:
        r10[r2] = r2;
        return;
    L_0x0097:
        r3 = r9.mBaseline;
        r3 = r3.isConnected();
        if (r3 == 0) goto L_0x00d3;
    L_0x009f:
        r1 = r9.mBaseline;
        r1 = r1.mTarget;
        r1 = r1.getOwner();
        r2 = r1.mVerticalWrapVisited;
        if (r2 != 0) goto L_0x00ae;
    L_0x00ab:
        r8.findVerticalWrapRecursive(r1, r10);
    L_0x00ae:
        r10 = r1.mDistToTop;
        r2 = r1.mHeight;
        r10 = r10 - r2;
        r10 = r10 + r0;
        r10 = java.lang.Math.max(r10, r0);
        r2 = r1.mDistToBottom;
        r1 = r1.mHeight;
        r2 = r2 - r1;
        r2 = r2 + r0;
        r0 = java.lang.Math.max(r2, r0);
        r1 = r9.getVisibility();
        if (r1 != r4) goto L_0x00ce;
    L_0x00c8:
        r1 = r9.mHeight;
        r10 = r10 - r1;
        r1 = r9.mHeight;
        r0 = r0 - r1;
    L_0x00ce:
        r9.mDistToTop = r10;
        r9.mDistToBottom = r0;
        return;
    L_0x00d3:
        r3 = r9.mTop;
        r3 = r3.isConnected();
        r5 = 0;
        if (r3 == 0) goto L_0x00f9;
    L_0x00dc:
        r3 = r9.mTop;
        r3 = r3.mTarget;
        r3 = r3.getOwner();
        r6 = r9.mTop;
        r6 = r6.getMargin();
        r6 = r6 + r0;
        r7 = r3.isRoot();
        if (r7 != 0) goto L_0x00fb;
    L_0x00f1:
        r7 = r3.mVerticalWrapVisited;
        if (r7 != 0) goto L_0x00fb;
    L_0x00f5:
        r8.findVerticalWrapRecursive(r3, r10);
        goto L_0x00fb;
    L_0x00f9:
        r6 = r0;
        r3 = r5;
    L_0x00fb:
        r7 = r9.mBottom;
        r7 = r7.isConnected();
        if (r7 == 0) goto L_0x011f;
    L_0x0103:
        r5 = r9.mBottom;
        r5 = r5.mTarget;
        r5 = r5.getOwner();
        r7 = r9.mBottom;
        r7 = r7.getMargin();
        r0 = r0 + r7;
        r7 = r5.isRoot();
        if (r7 != 0) goto L_0x011f;
    L_0x0118:
        r7 = r5.mVerticalWrapVisited;
        if (r7 != 0) goto L_0x011f;
    L_0x011c:
        r8.findVerticalWrapRecursive(r5, r10);
    L_0x011f:
        r10 = r9.mTop;
        r10 = r10.mTarget;
        if (r10 == 0) goto L_0x0193;
    L_0x0125:
        r10 = r3.isRoot();
        if (r10 != 0) goto L_0x0193;
    L_0x012b:
        r10 = r9.mTop;
        r10 = r10.mTarget;
        r10 = r10.getType();
        r7 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.TOP;
        if (r10 != r7) goto L_0x0140;
    L_0x0137:
        r10 = r3.mDistToTop;
        r7 = r3.getOptimizerWrapHeight();
        r10 = r10 - r7;
        r6 = r6 + r10;
        goto L_0x014f;
    L_0x0140:
        r10 = r9.mTop;
        r10 = r10.mTarget;
        r10 = r10.getType();
        r7 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.BOTTOM;
        if (r10 != r7) goto L_0x014f;
    L_0x014c:
        r10 = r3.mDistToTop;
        r6 = r6 + r10;
    L_0x014f:
        r10 = r3.mTopHasCentered;
        if (r10 != 0) goto L_0x0178;
    L_0x0153:
        r10 = r3.mTop;
        r10 = r10.mTarget;
        if (r10 == 0) goto L_0x0176;
    L_0x0159:
        r10 = r3.mTop;
        r10 = r10.mTarget;
        r10 = r10.mOwner;
        if (r10 == r9) goto L_0x0176;
    L_0x0161:
        r10 = r3.mBottom;
        r10 = r10.mTarget;
        if (r10 == 0) goto L_0x0176;
    L_0x0167:
        r10 = r3.mBottom;
        r10 = r10.mTarget;
        r10 = r10.mOwner;
        if (r10 == r9) goto L_0x0176;
    L_0x016f:
        r10 = r3.mVerticalDimensionBehaviour;
        r7 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        if (r10 == r7) goto L_0x0176;
    L_0x0175:
        goto L_0x0178;
    L_0x0176:
        r10 = r2;
        goto L_0x0179;
    L_0x0178:
        r10 = r1;
    L_0x0179:
        r9.mTopHasCentered = r10;
        r10 = r9.mTopHasCentered;
        if (r10 == 0) goto L_0x0193;
    L_0x017f:
        r10 = r3.mBottom;
        r10 = r10.mTarget;
        if (r10 != 0) goto L_0x0186;
    L_0x0185:
        goto L_0x018e;
    L_0x0186:
        r10 = r3.mBottom;
        r10 = r10.mTarget;
        r10 = r10.mOwner;
        if (r10 == r9) goto L_0x0193;
    L_0x018e:
        r10 = r3.mDistToTop;
        r10 = r6 - r10;
        r6 = r6 + r10;
    L_0x0193:
        r10 = r9.mBottom;
        r10 = r10.mTarget;
        if (r10 == 0) goto L_0x0205;
    L_0x0199:
        r10 = r5.isRoot();
        if (r10 != 0) goto L_0x0205;
    L_0x019f:
        r10 = r9.mBottom;
        r10 = r10.mTarget;
        r10 = r10.getType();
        r3 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.BOTTOM;
        if (r10 != r3) goto L_0x01b4;
    L_0x01ab:
        r10 = r5.mDistToBottom;
        r3 = r5.getOptimizerWrapHeight();
        r10 = r10 - r3;
        r0 = r0 + r10;
        goto L_0x01c3;
    L_0x01b4:
        r10 = r9.mBottom;
        r10 = r10.mTarget;
        r10 = r10.getType();
        r3 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.TOP;
        if (r10 != r3) goto L_0x01c3;
    L_0x01c0:
        r10 = r5.mDistToBottom;
        r0 = r0 + r10;
    L_0x01c3:
        r10 = r5.mBottomHasCentered;
        if (r10 != 0) goto L_0x01eb;
    L_0x01c7:
        r10 = r5.mTop;
        r10 = r10.mTarget;
        if (r10 == 0) goto L_0x01ea;
    L_0x01cd:
        r10 = r5.mTop;
        r10 = r10.mTarget;
        r10 = r10.mOwner;
        if (r10 == r9) goto L_0x01ea;
    L_0x01d5:
        r10 = r5.mBottom;
        r10 = r10.mTarget;
        if (r10 == 0) goto L_0x01ea;
    L_0x01db:
        r10 = r5.mBottom;
        r10 = r10.mTarget;
        r10 = r10.mOwner;
        if (r10 == r9) goto L_0x01ea;
    L_0x01e3:
        r10 = r5.mVerticalDimensionBehaviour;
        r3 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        if (r10 == r3) goto L_0x01ea;
    L_0x01e9:
        goto L_0x01eb;
    L_0x01ea:
        r1 = r2;
    L_0x01eb:
        r9.mBottomHasCentered = r1;
        r10 = r9.mBottomHasCentered;
        if (r10 == 0) goto L_0x0205;
    L_0x01f1:
        r10 = r5.mTop;
        r10 = r10.mTarget;
        if (r10 != 0) goto L_0x01f8;
    L_0x01f7:
        goto L_0x0200;
    L_0x01f8:
        r10 = r5.mTop;
        r10 = r10.mTarget;
        r10 = r10.mOwner;
        if (r10 == r9) goto L_0x0205;
    L_0x0200:
        r10 = r5.mDistToBottom;
        r10 = r0 - r10;
        r0 = r0 + r10;
    L_0x0205:
        r10 = r9.getVisibility();
        if (r10 != r4) goto L_0x0211;
    L_0x020b:
        r10 = r9.mHeight;
        r6 = r6 - r10;
        r10 = r9.mHeight;
        r0 = r0 - r10;
    L_0x0211:
        r9.mDistToTop = r6;
        r9.mDistToBottom = r0;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.constraint.solver.widgets.ConstraintWidgetContainer.findVerticalWrapRecursive(android.support.constraint.solver.widgets.ConstraintWidget, boolean[]):void");
    }

    public void findWrapSize(ArrayList<ConstraintWidget> arrayList, boolean[] zArr) {
        ConstraintWidgetContainer constraintWidgetContainer = this;
        ArrayList<ConstraintWidget> arrayList2 = arrayList;
        boolean[] zArr2 = zArr;
        int size = arrayList.size();
        int i = 0;
        zArr2[0] = true;
        int i2 = 0;
        int i3 = i2;
        int i4 = i3;
        int i5 = i4;
        int i6 = i5;
        int i7 = i6;
        int i8 = i7;
        while (i2 < size) {
            ConstraintWidget constraintWidget = (ConstraintWidget) arrayList2.get(i2);
            if (!constraintWidget.isRoot()) {
                if (!constraintWidget.mHorizontalWrapVisited) {
                    findHorizontalWrapRecursive(constraintWidget, zArr2);
                }
                if (!constraintWidget.mVerticalWrapVisited) {
                    findVerticalWrapRecursive(constraintWidget, zArr2);
                }
                if (zArr2[i]) {
                    int height = (constraintWidget.mDistToTop + constraintWidget.mDistToBottom) - constraintWidget.getHeight();
                    i = constraintWidget.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_PARENT ? (constraintWidget.getWidth() + constraintWidget.mLeft.mMargin) + constraintWidget.mRight.mMargin : (constraintWidget.mDistToLeft + constraintWidget.mDistToRight) - constraintWidget.getWidth();
                    int height2 = constraintWidget.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_PARENT ? (constraintWidget.getHeight() + constraintWidget.mTop.mMargin) + constraintWidget.mBottom.mMargin : height;
                    if (constraintWidget.getVisibility() == 8) {
                        i = 0;
                        height2 = 0;
                    }
                    i3 = Math.max(i3, constraintWidget.mDistToLeft);
                    i4 = Math.max(i4, constraintWidget.mDistToRight);
                    i7 = Math.max(i7, constraintWidget.mDistToBottom);
                    i6 = Math.max(i6, constraintWidget.mDistToTop);
                    i = Math.max(i5, i);
                    i8 = Math.max(i8, height2);
                    i5 = i;
                } else {
                    return;
                }
            }
            i2++;
            i = 0;
        }
        constraintWidgetContainer.mWrapWidth = Math.max(constraintWidgetContainer.mMinWidth, Math.max(Math.max(i3, i4), i5));
        constraintWidgetContainer.mWrapHeight = Math.max(constraintWidgetContainer.mMinHeight, Math.max(Math.max(i6, i7), i8));
        for (int i9 = 0; i9 < size; i9++) {
            ConstraintWidget constraintWidget2 = (ConstraintWidget) arrayList2.get(i9);
            constraintWidget2.mHorizontalWrapVisited = false;
            constraintWidget2.mVerticalWrapVisited = false;
            constraintWidget2.mLeftHasCentered = false;
            constraintWidget2.mRightHasCentered = false;
            constraintWidget2.mTopHasCentered = false;
            constraintWidget2.mBottomHasCentered = false;
        }
    }

    public int layoutFindGroups() {
        int i;
        r0 = new Type[5];
        int i2 = 0;
        r0[0] = Type.LEFT;
        r0[1] = Type.RIGHT;
        r0[2] = Type.TOP;
        r0[3] = Type.BASELINE;
        r0[4] = Type.BOTTOM;
        int size = this.mChildren.size();
        int i3 = 1;
        for (i = 0; i < size; i++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i);
            ConstraintAnchor constraintAnchor = constraintWidget.mLeft;
            if (constraintAnchor.mTarget == null) {
                constraintAnchor.mGroup = Integer.MAX_VALUE;
            } else if (setGroup(constraintAnchor, i3) == i3) {
                i3++;
            }
            constraintAnchor = constraintWidget.mTop;
            if (constraintAnchor.mTarget == null) {
                constraintAnchor.mGroup = Integer.MAX_VALUE;
            } else if (setGroup(constraintAnchor, i3) == i3) {
                i3++;
            }
            constraintAnchor = constraintWidget.mRight;
            if (constraintAnchor.mTarget == null) {
                constraintAnchor.mGroup = Integer.MAX_VALUE;
            } else if (setGroup(constraintAnchor, i3) == i3) {
                i3++;
            }
            constraintAnchor = constraintWidget.mBottom;
            if (constraintAnchor.mTarget == null) {
                constraintAnchor.mGroup = Integer.MAX_VALUE;
            } else if (setGroup(constraintAnchor, i3) == i3) {
                i3++;
            }
            ConstraintAnchor constraintAnchor2 = constraintWidget.mBaseline;
            if (constraintAnchor2.mTarget == null) {
                constraintAnchor2.mGroup = Integer.MAX_VALUE;
            } else if (setGroup(constraintAnchor2, i3) == i3) {
                i3++;
            }
        }
        i = 1;
        while (i != 0) {
            i = 0;
            i3 = i;
            while (i < size) {
                constraintWidget = (ConstraintWidget) this.mChildren.get(i);
                int i4 = i3;
                for (Type type : r0) {
                    ConstraintAnchor constraintAnchor3 = null;
                    switch (type) {
                        case LEFT:
                            constraintAnchor3 = constraintWidget.mLeft;
                            break;
                        case TOP:
                            constraintAnchor3 = constraintWidget.mTop;
                            break;
                        case RIGHT:
                            constraintAnchor3 = constraintWidget.mRight;
                            break;
                        case BOTTOM:
                            constraintAnchor3 = constraintWidget.mBottom;
                            break;
                        case BASELINE:
                            constraintAnchor3 = constraintWidget.mBaseline;
                            break;
                        default:
                            break;
                    }
                    ConstraintAnchor constraintAnchor4 = constraintAnchor3.mTarget;
                    if (constraintAnchor4 != null) {
                        if (!(constraintAnchor4.mOwner.getParent() == null || constraintAnchor4.mGroup == constraintAnchor3.mGroup)) {
                            i4 = constraintAnchor3.mGroup > constraintAnchor4.mGroup ? constraintAnchor4.mGroup : constraintAnchor3.mGroup;
                            constraintAnchor3.mGroup = i4;
                            constraintAnchor4.mGroup = i4;
                            i4 = 1;
                        }
                        constraintAnchor4 = constraintAnchor4.getOpposite();
                        if (!(constraintAnchor4 == null || constraintAnchor4.mGroup == constraintAnchor3.mGroup)) {
                            i4 = constraintAnchor3.mGroup > constraintAnchor4.mGroup ? constraintAnchor4.mGroup : constraintAnchor3.mGroup;
                            constraintAnchor3.mGroup = i4;
                            constraintAnchor4.mGroup = i4;
                            i4 = 1;
                        }
                    }
                }
                i++;
                i3 = i4;
            }
            i = i3;
        }
        int[] iArr = new int[((this.mChildren.size() * r0.length) + 1)];
        Arrays.fill(iArr, -1);
        int i5 = 0;
        while (i2 < size) {
            ConstraintWidget constraintWidget2 = (ConstraintWidget) this.mChildren.get(i2);
            constraintAnchor2 = constraintWidget2.mLeft;
            if (constraintAnchor2.mGroup != Integer.MAX_VALUE) {
                i4 = constraintAnchor2.mGroup;
                if (iArr[i4] == -1) {
                    int i6 = i5 + 1;
                    iArr[i4] = i5;
                    i5 = i6;
                }
                constraintAnchor2.mGroup = iArr[i4];
            }
            constraintAnchor2 = constraintWidget2.mTop;
            if (constraintAnchor2.mGroup != Integer.MAX_VALUE) {
                i4 = constraintAnchor2.mGroup;
                if (iArr[i4] == -1) {
                    i6 = i5 + 1;
                    iArr[i4] = i5;
                    i5 = i6;
                }
                constraintAnchor2.mGroup = iArr[i4];
            }
            constraintAnchor2 = constraintWidget2.mRight;
            if (constraintAnchor2.mGroup != Integer.MAX_VALUE) {
                i4 = constraintAnchor2.mGroup;
                if (iArr[i4] == -1) {
                    i6 = i5 + 1;
                    iArr[i4] = i5;
                    i5 = i6;
                }
                constraintAnchor2.mGroup = iArr[i4];
            }
            constraintAnchor2 = constraintWidget2.mBottom;
            if (constraintAnchor2.mGroup != Integer.MAX_VALUE) {
                i4 = constraintAnchor2.mGroup;
                if (iArr[i4] == -1) {
                    i6 = i5 + 1;
                    iArr[i4] = i5;
                    i5 = i6;
                }
                constraintAnchor2.mGroup = iArr[i4];
            }
            ConstraintAnchor constraintAnchor5 = constraintWidget2.mBaseline;
            if (constraintAnchor5.mGroup != Integer.MAX_VALUE) {
                int i7 = constraintAnchor5.mGroup;
                if (iArr[i7] == -1) {
                    i4 = i5 + 1;
                    iArr[i7] = i5;
                    i5 = i4;
                }
                constraintAnchor5.mGroup = iArr[i7];
            }
            i2++;
        }
        return i5;
    }

    public void layoutWithGroup(int i) {
        int i2 = this.mX;
        int i3 = this.mY;
        int i4 = 0;
        if (this.mParent != null) {
            if (this.mSnapshot == null) {
                this.mSnapshot = new Snapshot(this);
            }
            this.mSnapshot.updateFrom(this);
            this.mX = 0;
            this.mY = 0;
            resetAnchors();
            resetSolverVariables(this.mSystem.getCache());
        } else {
            this.mX = 0;
            this.mY = 0;
        }
        int size = this.mChildren.size();
        for (int i5 = 0; i5 < size; i5++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i5);
            if (constraintWidget instanceof WidgetContainer) {
                ((WidgetContainer) constraintWidget).layout();
            }
        }
        this.mLeft.mGroup = 0;
        this.mRight.mGroup = 0;
        this.mTop.mGroup = 1;
        this.mBottom.mGroup = 1;
        this.mSystem.reset();
        while (i4 < i) {
            try {
                addToSolver(this.mSystem, i4);
                this.mSystem.minimize();
                updateFromSolver(this.mSystem, i4);
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateFromSolver(this.mSystem, -2);
            i4++;
        }
        if (this.mParent != 0) {
            i = getWidth();
            i2 = getHeight();
            this.mSnapshot.applyTo(this);
            setWidth(i);
            setHeight(i2);
        } else {
            this.mX = i2;
            this.mY = i3;
        }
        if (this == getRootConstraintContainer()) {
            updateDrawPosition();
        }
    }

    public ArrayList<Guideline> getVerticalGuidelines() {
        ArrayList<Guideline> arrayList = new ArrayList();
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i);
            if (constraintWidget instanceof Guideline) {
                Guideline guideline = (Guideline) constraintWidget;
                if (guideline.getOrientation() == 1) {
                    arrayList.add(guideline);
                }
            }
        }
        return arrayList;
    }

    public ArrayList<Guideline> getHorizontalGuidelines() {
        ArrayList<Guideline> arrayList = new ArrayList();
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i);
            if (constraintWidget instanceof Guideline) {
                Guideline guideline = (Guideline) constraintWidget;
                if (guideline.getOrientation() == 0) {
                    arrayList.add(guideline);
                }
            }
        }
        return arrayList;
    }

    public LinearSystem getSystem() {
        return this.mSystem;
    }

    private void resetChains() {
        this.mHorizontalChainsSize = 0;
        this.mVerticalChainsSize = 0;
    }

    void addChain(ConstraintWidget constraintWidget, int i) {
        if (i == 0) {
            while (constraintWidget.mLeft.mTarget != 0 && constraintWidget.mLeft.mTarget.mOwner.mRight.mTarget != 0 && constraintWidget.mLeft.mTarget.mOwner.mRight.mTarget == constraintWidget.mLeft && constraintWidget.mLeft.mTarget.mOwner != constraintWidget) {
                constraintWidget = constraintWidget.mLeft.mTarget.mOwner;
            }
            addHorizontalChain(constraintWidget);
        } else if (i == 1) {
            while (constraintWidget.mTop.mTarget != 0 && constraintWidget.mTop.mTarget.mOwner.mBottom.mTarget != 0 && constraintWidget.mTop.mTarget.mOwner.mBottom.mTarget == constraintWidget.mTop && constraintWidget.mTop.mTarget.mOwner != constraintWidget) {
                constraintWidget = constraintWidget.mTop.mTarget.mOwner;
            }
            addVerticalChain(constraintWidget);
        }
    }

    private void addHorizontalChain(ConstraintWidget constraintWidget) {
        int i = 0;
        while (i < this.mHorizontalChainsSize) {
            if (this.mHorizontalChainsArray[i] != constraintWidget) {
                i++;
            } else {
                return;
            }
        }
        if (this.mHorizontalChainsSize + 1 >= this.mHorizontalChainsArray.length) {
            this.mHorizontalChainsArray = (ConstraintWidget[]) Arrays.copyOf(this.mHorizontalChainsArray, this.mHorizontalChainsArray.length * 2);
        }
        this.mHorizontalChainsArray[this.mHorizontalChainsSize] = constraintWidget;
        this.mHorizontalChainsSize++;
    }

    private void addVerticalChain(ConstraintWidget constraintWidget) {
        int i = 0;
        while (i < this.mVerticalChainsSize) {
            if (this.mVerticalChainsArray[i] != constraintWidget) {
                i++;
            } else {
                return;
            }
        }
        if (this.mVerticalChainsSize + 1 >= this.mVerticalChainsArray.length) {
            this.mVerticalChainsArray = (ConstraintWidget[]) Arrays.copyOf(this.mVerticalChainsArray, this.mVerticalChainsArray.length * 2);
        }
        this.mVerticalChainsArray[this.mVerticalChainsSize] = constraintWidget;
        this.mVerticalChainsSize++;
    }

    private int countMatchConstraintsChainedWidgets(LinearSystem linearSystem, ConstraintWidget[] constraintWidgetArr, ConstraintWidget constraintWidget, int i, boolean[] zArr) {
        int i2;
        ConstraintWidget constraintWidget2 = this;
        LinearSystem linearSystem2 = linearSystem;
        ConstraintWidget constraintWidget3 = constraintWidget;
        zArr[0] = true;
        zArr[1] = false;
        ConstraintWidget constraintWidget4 = null;
        constraintWidgetArr[0] = null;
        constraintWidgetArr[2] = null;
        constraintWidgetArr[1] = null;
        constraintWidgetArr[3] = null;
        float f = 0.0f;
        int i3 = 5;
        int i4 = 8;
        ConstraintWidget constraintWidget5;
        ConstraintWidget constraintWidget6;
        int i5;
        int i6;
        if (i == 0) {
            boolean z = constraintWidget3.mLeft.mTarget == null || constraintWidget3.mLeft.mTarget.mOwner == constraintWidget2;
            constraintWidget3.mHorizontalNextWidget = null;
            i2 = 0;
            ConstraintWidget constraintWidget7 = null;
            constraintWidget5 = constraintWidget.getVisibility() != 8 ? constraintWidget3 : null;
            ConstraintWidget constraintWidget8 = constraintWidget5;
            constraintWidget6 = constraintWidget3;
            while (constraintWidget6.mRight.mTarget != null) {
                constraintWidget6.mHorizontalNextWidget = constraintWidget4;
                if (constraintWidget6.getVisibility() != 8) {
                    if (constraintWidget8 == null) {
                        constraintWidget8 = constraintWidget6;
                    }
                    if (!(constraintWidget5 == null || constraintWidget5 == constraintWidget6)) {
                        constraintWidget5.mHorizontalNextWidget = constraintWidget6;
                    }
                    constraintWidget5 = constraintWidget6;
                } else {
                    linearSystem2.addEquality(constraintWidget6.mLeft.mSolverVariable, constraintWidget6.mLeft.mTarget.mSolverVariable, 0, 5);
                    linearSystem2.addEquality(constraintWidget6.mRight.mSolverVariable, constraintWidget6.mLeft.mSolverVariable, 0, 5);
                }
                if (constraintWidget6.getVisibility() != 8 && constraintWidget6.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                    if (constraintWidget6.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                        zArr[0] = false;
                    }
                    if (constraintWidget6.mDimensionRatio <= f) {
                        zArr[0] = false;
                        i5 = i2 + 1;
                        if (i5 >= constraintWidget2.mMatchConstraintsChainedWidgets.length) {
                            constraintWidget2.mMatchConstraintsChainedWidgets = (ConstraintWidget[]) Arrays.copyOf(constraintWidget2.mMatchConstraintsChainedWidgets, constraintWidget2.mMatchConstraintsChainedWidgets.length * 2);
                        }
                        constraintWidget2.mMatchConstraintsChainedWidgets[i2] = constraintWidget6;
                        i2 = i5;
                    }
                }
                if (constraintWidget6.mRight.mTarget.mOwner.mLeft.mTarget == null) {
                    break;
                } else if (constraintWidget6.mRight.mTarget.mOwner.mLeft.mTarget.mOwner != constraintWidget6) {
                    break;
                } else if (constraintWidget6.mRight.mTarget.mOwner == constraintWidget6) {
                    break;
                } else {
                    constraintWidget7 = constraintWidget6.mRight.mTarget.mOwner;
                    constraintWidget6 = constraintWidget7;
                    constraintWidget4 = null;
                    f = 0.0f;
                }
            }
            if (!(constraintWidget6.mRight.mTarget == null || constraintWidget6.mRight.mTarget.mOwner == constraintWidget2)) {
                z = false;
            }
            if (constraintWidget3.mLeft.mTarget != null) {
                if (constraintWidget7.mRight.mTarget != null) {
                    i6 = 1;
                    constraintWidget3.mHorizontalChainFixedPosition = z;
                    constraintWidget7.mHorizontalNextWidget = null;
                    constraintWidgetArr[0] = constraintWidget3;
                    constraintWidgetArr[2] = constraintWidget8;
                    constraintWidgetArr[i6] = constraintWidget7;
                    constraintWidgetArr[3] = constraintWidget5;
                }
            }
            i6 = 1;
            zArr[1] = true;
            constraintWidget3.mHorizontalChainFixedPosition = z;
            constraintWidget7.mHorizontalNextWidget = null;
            constraintWidgetArr[0] = constraintWidget3;
            constraintWidgetArr[2] = constraintWidget8;
            constraintWidgetArr[i6] = constraintWidget7;
            constraintWidgetArr[3] = constraintWidget5;
        } else {
            boolean z2 = constraintWidget3.mTop.mTarget == null || constraintWidget3.mTop.mTarget.mOwner == constraintWidget2;
            constraintWidget4 = null;
            constraintWidget3.mVerticalNextWidget = null;
            int i7 = 0;
            constraintWidget6 = null;
            constraintWidget5 = constraintWidget.getVisibility() != 8 ? constraintWidget3 : null;
            ConstraintWidget constraintWidget9 = constraintWidget5;
            ConstraintWidget constraintWidget10 = constraintWidget3;
            while (constraintWidget10.mBottom.mTarget != null) {
                constraintWidget10.mVerticalNextWidget = constraintWidget4;
                if (constraintWidget10.getVisibility() != i4) {
                    if (constraintWidget5 == null) {
                        constraintWidget5 = constraintWidget10;
                    }
                    if (!(constraintWidget9 == null || constraintWidget9 == constraintWidget10)) {
                        constraintWidget9.mVerticalNextWidget = constraintWidget10;
                    }
                    constraintWidget9 = constraintWidget10;
                } else {
                    linearSystem2.addEquality(constraintWidget10.mTop.mSolverVariable, constraintWidget10.mTop.mTarget.mSolverVariable, 0, i3);
                    linearSystem2.addEquality(constraintWidget10.mBottom.mSolverVariable, constraintWidget10.mTop.mSolverVariable, 0, i3);
                }
                if (constraintWidget10.getVisibility() != i4 && constraintWidget10.mVerticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                    if (constraintWidget10.mHorizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
                        zArr[0] = false;
                    }
                    if (constraintWidget10.mDimensionRatio <= 0.0f) {
                        zArr[0] = false;
                        i5 = i7 + 1;
                        if (i5 >= constraintWidget2.mMatchConstraintsChainedWidgets.length) {
                            constraintWidget2.mMatchConstraintsChainedWidgets = (ConstraintWidget[]) Arrays.copyOf(constraintWidget2.mMatchConstraintsChainedWidgets, constraintWidget2.mMatchConstraintsChainedWidgets.length * 2);
                        }
                        constraintWidget2.mMatchConstraintsChainedWidgets[i7] = constraintWidget10;
                        i7 = i5;
                    }
                }
                if (constraintWidget10.mBottom.mTarget.mOwner.mTop.mTarget != null) {
                    if (constraintWidget10.mBottom.mTarget.mOwner.mTop.mTarget.mOwner == constraintWidget10) {
                        if (constraintWidget10.mBottom.mTarget.mOwner != constraintWidget10) {
                            constraintWidget6 = constraintWidget10.mBottom.mTarget.mOwner;
                            constraintWidget10 = constraintWidget6;
                            constraintWidget4 = null;
                            i3 = 5;
                            i4 = 8;
                        }
                    }
                }
            }
            i2 = i7;
            if (!(constraintWidget10.mBottom.mTarget == null || constraintWidget10.mBottom.mTarget.mOwner == constraintWidget2)) {
                z2 = false;
            }
            if (constraintWidget3.mTop.mTarget != null) {
                if (constraintWidget6.mBottom.mTarget != null) {
                    i6 = 1;
                    constraintWidget3.mVerticalChainFixedPosition = z2;
                    constraintWidget6.mVerticalNextWidget = null;
                    constraintWidgetArr[0] = constraintWidget3;
                    constraintWidgetArr[2] = constraintWidget5;
                    constraintWidgetArr[i6] = constraintWidget6;
                    constraintWidgetArr[3] = constraintWidget9;
                }
            }
            i6 = 1;
            zArr[1] = true;
            constraintWidget3.mVerticalChainFixedPosition = z2;
            constraintWidget6.mVerticalNextWidget = null;
            constraintWidgetArr[0] = constraintWidget3;
            constraintWidgetArr[2] = constraintWidget5;
            constraintWidgetArr[i6] = constraintWidget6;
            constraintWidgetArr[3] = constraintWidget9;
        }
        return i2;
    }
}
