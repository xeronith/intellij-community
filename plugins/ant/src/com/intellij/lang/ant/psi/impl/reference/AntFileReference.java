package com.intellij.lang.ant.psi.impl.reference;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.ant.AntBundle;
import com.intellij.lang.ant.psi.AntProperty;
import com.intellij.lang.ant.psi.AntStructuredElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceType;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AntFileReference extends FileReferenceBase implements AntReference {

  public AntFileReference(final AntFileReferenceSet set, final TextRange range, final int index, final String text) {
    super(set, range, index, text);
  }

  @Nullable
  protected String getText() {
    return getElement().computeAttributeValue(super.getText());
  }

  public AntStructuredElement getElement() {
    return (AntStructuredElement)super.getElement();
  }

  @NotNull
  public AntFileReferenceSet getFileReferenceSet() {
    return (AntFileReferenceSet)super.getFileReferenceSet();
  }

  public ReferenceType getSoftenType() {
    return new ReferenceType(new int[]{ReferenceType.FILE, ReferenceType.DIRECTORY});
  }

  public String getUnresolvedMessagePattern() {
    return AntBundle.message("file.doesnt.exist", getCanonicalRepresentationText());
  }

  public boolean shouldBeSkippedByAnnotator() {
    final AntStructuredElement element = getElement();
    return (element instanceof AntProperty) && ((AntProperty)element).getFileName() != null;
  }

  public void setShouldBeSkippedByAnnotator(boolean value) {
  }

  @NotNull
  public IntentionAction[] getFixes() {
    return EMPTY_INTENTIONS;
  }

  @Nullable
  public String getCanonicalRepresentationText() {
    final AntStructuredElement element = getElement();
    final String value = getCanonicalText();
    return element.computeAttributeValue(value);
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final AntStructuredElement antElement = getElement();
    final PsiElement element = getManipulatorElement();
    getManipulator(element).handleContentChange(element, getRangeInElement().shiftRight(
      antElement.getTextRange().getStartOffset() - element.getTextRange().getStartOffset()), newElementName);
    return antElement;
  }

  public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException {
    if (element instanceof PsiNamedElement) {
      return handleElementRename(((PsiNamedElement)element).getName());
    }
    return getElement();
  }

  protected ResolveResult[] innerResolve() {
    final ResolveResult[] resolveResult = super.innerResolve();
    if (resolveResult.length == 0) {
      final String text = getText();
      if (text != null && text.length() > 0 && getFileReferenceSet().isAbsolutePathReference()) {
        final VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(text);
        if (dir != null) {
          final PsiDirectory psiDir = getElement().getManager().findDirectory(dir);
          if (psiDir != null) {
            return new ResolveResult[]{new PsiElementResolveResult(psiDir)};
          }
        }
      }
    }
    return resolveResult;
  }

  private PsiElement getManipulatorElement() {
    return getFileReferenceSet().getManipulatorElement();
  }
}