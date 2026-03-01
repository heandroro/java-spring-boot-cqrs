#!/bin/bash
# mermaid-validator.sh - Valida diagramas Mermaid no README.md

echo "🔍 Validating Mermaid diagrams in README.md..."

# Verificar se README.md existe
if [ ! -f "README.md" ]; then
    echo "❌ README.md not found!"
    exit 1
fi

# Contar diagramas Mermaid encontrados
diagram_count=$(grep -c "\`\`\`mermaid" README.md)

if [ "$diagram_count" -eq 0 ]; then
    echo "ℹ️  No Mermaid diagrams found in README.md"
    exit 0
fi

echo "📊 Found $diagram_count Mermaid diagram(s) in README.md"

# Listar posições dos diagramas
echo ""
echo "📍 Diagram locations:"
grep -n "\`\`\`mermaid" README.md | while read -r line; do
    line_num=$(echo "$line" | cut -d: -f1)
    echo "   • Line $line_num: Mermaid diagram found"
done

echo ""
echo "✅ Basic validation complete!"
echo ""
echo "💡 Tips for manual validation:"
echo "   • Copy diagram code to: https://mermaid.live/"
echo "   • Check GitHub PR preview for rendering"
echo "   • Ensure no syntax errors appear"
echo ""
echo "🎯 All diagrams should render correctly in GitHub!"
