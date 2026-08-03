// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  docsSidebar: [
    'intro',
    {
      type: 'category',
      label: 'Getting Started',
      items: [
        'getting-started/overview',
        'getting-started/installation',
        'getting-started/first-story',
      ],
    },
    {
      type: 'category',
      label: 'Guides',
      items: [
        'guides/desktop-catalog',
        'guides/web-catalog',
        'guides/multi-module-setup',
        'guides/component-references',
        'guides/component-documentation',
        'guides/controls-and-knobs',
        'guides/theming',
        'guides/web-export',
        'guides/local-publishing',
      ],
    },
    {
      type: 'category',
      label: 'Reference',
      items: [
        'reference/annotations',
        'reference/gradle-plugins',
        'reference/tasks-and-commands',
        'reference/constraints-and-limitations',
      ],
    },
  ],
};

export default sidebars;
